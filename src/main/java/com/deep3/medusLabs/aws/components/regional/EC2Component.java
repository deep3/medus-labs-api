package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.waiters.WaiterParameters;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EC2Component implements AWSRegionalServiceComponentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(EC2Component.class);

    private final AmazonEC2 ec2Client;

    @Autowired
    DeployedLabLogService deployedLabLogService;

    public EC2Component(STSAssumeRoleSessionCredentialsProvider credentials, Regions region) throws InvalidAWSCredentials {
        if(credentials == null){
            throw new InvalidAWSCredentials();
        }
        this.ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(credentials).withRegion(region).build();

    }


    /**
     * Creates a key pair in EC2 and returns the private key back as a string
     *
     * Its important that the string is not thrown away as this is the only time
     * you will be able to retrieve it.
     *
     * @param keyName
     * @return
     */
    public String createKeyPair(String keyName){

        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest().withKeyName(keyName);
        CreateKeyPairResult createKeyPairResult =  ec2Client.createKeyPair(createKeyPairRequest);
        KeyPair keyPair = createKeyPairResult.getKeyPair();
        return keyPair.getKeyMaterial();
    }

    @Override
    public void deleteAll(long deployedLabId) {
        deleteAllEc2Components(deployedLabId);
        deleteAllVpcComponents(deployedLabId);
    }

    /**
     * All components available in the EC2 Dashboard
     */
    public void deleteAllEc2Components(long deployedLabId) {
        deleteInstances(deployedLabId);
        deleteKeyPairs(deployedLabId);
        deleteSecurityGroups(deployedLabId);
        deleteAMIsAndSnapshots(deployedLabId);
        deleteVolumes(deployedLabId);
    }

    /**
     * All components available in the VPC dashboard
     */
    public void deleteAllVpcComponents(long deployedLabId) {
        deleteNatGateways(deployedLabId);
        deleteElasticIps(deployedLabId);
        deleteSubnets(deployedLabId);
        deleteRouteTables(deployedLabId);
        deleteNetworkAcls(deployedLabId);
        deleteInternetGateways(deployedLabId);
        deleteVPC(deployedLabId);
    }

    public void deleteInstances(long deployedLabId) {
        // Collect all instances
        DescribeInstancesResult reservationResult = ec2Client.describeInstances(new DescribeInstancesRequest());
        List<Reservation> reservations = reservationResult.getReservations();

        while (reservationResult.getNextToken() != null) {
            reservationResult = ec2Client.describeInstances(new DescribeInstancesRequest()
                    .withNextToken(reservationResult.getNextToken()));
            reservations.addAll(reservationResult.getReservations());
        }

        // Collect instance Ids
        List<String> instanceIds = new ArrayList<>();
        for (Reservation reservation : reservations) {
            for (Instance instance : reservation.getInstances()) {
                // Instance must not be shutting-down or terminated
                if (instance.getState().getCode() != 48 && instance.getState().getCode() != 32) {
                    try {
                        // Make sure termination protection is turned off
                        ec2Client.modifyInstanceAttribute(new ModifyInstanceAttributeRequest()
                                .withInstanceId(instance.getInstanceId())
                                .withDisableApiTermination(false));
                    } catch (Exception e) {
                        deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                                "Failed to disable termination protection for instance [%s]: %s", instance.getInstanceId(), e.getMessage());
                        LOG.error("Failed to disable termination protection for instance [{}]: {}", instance.getInstanceId(), e.getMessage());
                    }
                    instanceIds.add(instance.getInstanceId());
                }
            }
        }
        if (instanceIds.size() > 0) {
            try {
                // Bulk delete call to delete all instances
                ec2Client.terminateInstances(new TerminateInstancesRequest()
                        .withInstanceIds(instanceIds));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Error deleting instances {} : {}", String.join(",", instanceIds), e.getMessage());
                LOG.error("Error deleting instances {} : {}", instanceIds, e.getMessage());
            }

            // Wait for instances to delete
            ec2Client.waiters().instanceTerminated().run(new WaiterParameters<>(new DescribeInstancesRequest()
                    .withInstanceIds(instanceIds)));
        }
    }

    public void deleteKeyPairs(long deployedLabId) {
        List<KeyPairInfo> keyPairs = ec2Client.describeKeyPairs().getKeyPairs();

        for (KeyPairInfo keyPair : keyPairs) {
            try {
                ec2Client.deleteKeyPair(new DeleteKeyPairRequest().withKeyName(keyPair.getKeyName()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete KeyPair [%s]: %s", keyPair.getKeyName(), e.getMessage());
                LOG.error("Failed to delete KeyPair [{}]: {}", keyPair.getKeyName(), e.getMessage());
            }
        }
    }

    public void deleteSecurityGroups(long deployedLabId) {
        DescribeSecurityGroupsResult securityGroupsResult = ec2Client.describeSecurityGroups();
        List<SecurityGroup> securityGroups = securityGroupsResult.getSecurityGroups();

        while (securityGroupsResult.getNextToken() != null) {
            securityGroupsResult = ec2Client.describeSecurityGroups(new DescribeSecurityGroupsRequest()
                    .withNextToken(securityGroupsResult.getNextToken()));
            securityGroups.addAll(securityGroupsResult.getSecurityGroups());
        }

        for (SecurityGroup securityGroup : securityGroups) {
            if (securityGroup.getGroupName().equals("default")) {
                // Remove all permissions from the default group to return it to a clean state
                if (securityGroup.getIpPermissions().size() != 0) {
                    try {
                        ec2Client.revokeSecurityGroupIngress(new RevokeSecurityGroupIngressRequest()
                                .withGroupId(securityGroup.getGroupId())
                                .withIpPermissions(securityGroup.getIpPermissions()));
                    } catch (Exception e) {
                        deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                                "Failed to remove Ingress permissions from security group [%s]: %s", securityGroup.getGroupId(), e.getMessage());
                        LOG.error("Failed to remove Ingress permissions from security group [{}]: {}", securityGroup.getGroupId(), e.getMessage());
                    }
                }
                if (securityGroup.getIpPermissionsEgress().size() != 0) {
                    try {
                        ec2Client.revokeSecurityGroupEgress(new RevokeSecurityGroupEgressRequest()
                                .withGroupId(securityGroup.getGroupId())
                                .withIpPermissions(securityGroup.getIpPermissionsEgress()));
                    } catch (Exception e) {
                        deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                                "Failed to remove Egress permissions from security group [%s]: %s", securityGroup.getGroupId(), e.getMessage());
                        LOG.error("Failed to remove Egress permissions from security group [{}]: {}", securityGroup.getGroupId(), e.getMessage());
                    }
                }
            } else {
                try {
                    // delete everything that isn't the default group
                    ec2Client.deleteSecurityGroup(new DeleteSecurityGroupRequest()
                            .withGroupId(securityGroup.getGroupId()));
                } catch (Exception e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to delete security group [%s]: %s", securityGroup.getGroupId(), e.getMessage());
                    LOG.error("Failed to delete security group [{}]: {}", securityGroup.getGroupId(), e.getMessage());
                }
            }
        }
    }

    public void deleteAMIsAndSnapshots(long deployedLabId) {
        final String self = "self"; // This ensures the results returned are owned by and therefore authorized to be deleted by this account
        List<Image> images = ec2Client.describeImages(new DescribeImagesRequest().withOwners(self)).getImages();

        for (Image image : images) {
            try {
                ec2Client.deregisterImage(new DeregisterImageRequest()
                        .withImageId(image.getImageId()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to deregister image [%s]: %s", image.getImageId(), e.getMessage());
                LOG.error("Failed to deregister image [{}]: {}", image.getImageId(), e.getMessage());
            }
        }

        DescribeSnapshotsResult snapshotsResult = ec2Client.describeSnapshots(new DescribeSnapshotsRequest()
                .withOwnerIds(self));
        List<Snapshot> snapshots = snapshotsResult.getSnapshots();

        while (snapshotsResult.getNextToken() != null) {
            snapshotsResult = ec2Client.describeSnapshots(new DescribeSnapshotsRequest()
                    .withOwnerIds(self)
                    .withNextToken(snapshotsResult.getNextToken()));
            snapshots.addAll(snapshotsResult.getSnapshots());
        }

        for (Snapshot snapshot : snapshots) {
            try {
                ec2Client.deleteSnapshot(new DeleteSnapshotRequest()
                        .withSnapshotId(snapshot.getSnapshotId()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete snapshot [%s]: %s", snapshot.getSnapshotId(), e.getMessage());
                LOG.error("Failed to delete snapshot [{}]: {}", snapshot.getSnapshotId(), e.getMessage());
            }
        }
    }

    public void deleteVolumes(long deployedLabId) {
        DescribeVolumesResult volumesResult = ec2Client.describeVolumes();
        List<Volume> volumes = volumesResult.getVolumes();

        while (volumesResult.getNextToken() != null) {
            volumesResult = ec2Client.describeVolumes(new DescribeVolumesRequest()
                    .withNextToken(volumesResult.getNextToken()));
            volumes.addAll(volumesResult.getVolumes());
        }

        for (Volume volume : volumes) {
            try {
                ec2Client.deleteVolume(new DeleteVolumeRequest()
                        .withVolumeId(volume.getVolumeId()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete volume [%s]: %s", volume.getVolumeId(), e.getMessage());
                LOG.error("Failed to delete volume [{}]: {}", volume.getVolumeId(), e.getMessage());
            }
        }
    }

    public void deleteElasticIps(long deployedLabId) {
        List<Address> addresses = ec2Client.describeAddresses().getAddresses();

        for (Address address : addresses) {
            try {
                // EIPs must be detached beforehand - this should be done before reaching this stage
                ec2Client.releaseAddress(new ReleaseAddressRequest()
                        .withAllocationId(address.getAllocationId()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete Elastic IP Address [%s]: %s", address.getPublicIp(), e.getMessage());
                LOG.error("Failed to delete Elastic IP Address [{}]: {}", address.getPublicIp(), e.getMessage());
            }
        }
    }

    public void deleteNatGateways(long deployedLabId) {
        List<NatGateway> natGateways = ec2Client.describeNatGateways(new DescribeNatGatewaysRequest()).getNatGateways();

        for (NatGateway natGateway : natGateways) {
            // Only delete active NAT gateways
            if (!natGateway.getState().equals("deleted") && !natGateway.getState().equals("deleting") ) {
                try {
                    ec2Client.deleteNatGateway(new DeleteNatGatewayRequest()
                            .withNatGatewayId(natGateway.getNatGatewayId()));
                } catch (Exception e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to delete NAT Gateway [%s]: %s", natGateway.getNatGatewayId(), e.getMessage());
                    LOG.error("Failed to delete NAT Gateway [{}]: {}", natGateway.getNatGatewayId(), e.getMessage());
                }
            }
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(() -> checkNatDeleteSuccessful(countDownLatch), 0, 2, TimeUnit.SECONDS);

        try {
            countDownLatch.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                    "Wait for NAT gateway deletion interrupted: %s", e.getMessage());
            LOG.error("Wait for NAT gateway deletion interrupted: {}", e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Checks to see if all NAT gateways have entered a deleted state, signals the count down latch when this is true.
     * @param countDownLatch the latch to signal all NATs have entered the desired state
     */
    private void checkNatDeleteSuccessful(CountDownLatch countDownLatch) {
        boolean deleted = true;
        List<NatGateway> natGateways = ec2Client.describeNatGateways(new DescribeNatGatewaysRequest()).getNatGateways();
        for (NatGateway natGateway : natGateways) {
            if (!natGateway.getState().equals("deleted")) {
                deleted = false;
            }
        }
        if (deleted) {
            countDownLatch.countDown();
        }
    }

    public void deleteVPC(long deployedLabId) {
        List<Vpc> vpcs = ec2Client.describeVpcs().getVpcs();

        for (Vpc vpc : vpcs) {
            try {
                ec2Client.deleteVpc(new DeleteVpcRequest().withVpcId(vpc.getVpcId()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete VPC [%s]: %s", vpc.getVpcId(), e.getMessage());
                LOG.error("Failed to delete VPC [{}]: {}", vpc.getVpcId(), e.getMessage());
            }
        }
    }

    public void deleteSubnets(long deployedLabId) {
        List<Subnet> subnets = ec2Client.describeSubnets().getSubnets();

        for (Subnet subnet : subnets) {
            try {
                ec2Client.deleteSubnet(new DeleteSubnetRequest()
                        .withSubnetId(subnet.getSubnetId()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete subnet [%s]: %s", subnet.getSubnetId(), e.getMessage());
                LOG.error("Failed to delete subnet [{}]: {}", subnet.getSubnetId(), e.getMessage());
            }
        }
    }

    public void deleteRouteTables(long deployedLabId) {
        List<RouteTable> routeTables = ec2Client.describeRouteTables().getRouteTables();

        for (RouteTable routeTable : routeTables) {
            // Providing subnets have been deleted all but the default tables will have 0 associations
            // We can't delete the defaults so we use association size as an indicator for them
            if (routeTable.getAssociations().size() == 0) {
                try {
                    ec2Client.deleteRouteTable(new DeleteRouteTableRequest()
                            .withRouteTableId(routeTable.getRouteTableId()));
                } catch (Exception e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to delete route table [%s]: %s", routeTable.getRouteTableId(), e.getMessage());
                    LOG.error("Failed to delete route table [{}]: {}", routeTable.getRouteTableId(), e.getMessage());
                }
            }
        }
    }

    public void deleteInternetGateways(long deployedLabId) {
        List<InternetGateway> internetGateways = ec2Client.describeInternetGateways().getInternetGateways();

        for (InternetGateway internetGateway : internetGateways) {
            for (InternetGatewayAttachment internetGatewayAttachment : internetGateway.getAttachments()) {
                try {
                    // Detach gateways from vpcs first
                    ec2Client.detachInternetGateway(new DetachInternetGatewayRequest()
                            .withInternetGatewayId(internetGateway.getInternetGatewayId())
                            .withVpcId(internetGatewayAttachment.getVpcId()));
                } catch (Exception e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to detach internet gateway [%s] from vpc [%s]: %s", internetGateway.getInternetGatewayId(),
                            internetGatewayAttachment.getVpcId(), e.getMessage());
                    LOG.error("Failed to detach internet gateway [{}] from vpc [{}]: {}", internetGateway.getInternetGatewayId(), internetGatewayAttachment.getVpcId(), e.getMessage());
                }
            }
            try {
                ec2Client.deleteInternetGateway(new DeleteInternetGatewayRequest()
                        .withInternetGatewayId(internetGateway.getInternetGatewayId()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete internet gateway [%s]: %s", internetGateway.getInternetGatewayId(), e.getMessage());
                LOG.error("Failed to delete internet gateway [{}]: {}", internetGateway.getInternetGatewayId(), e.getMessage());
            }
        }
    }

    public void deleteNetworkAcls(long deployedLabId) {
        List<NetworkAcl> networkAcls = ec2Client.describeNetworkAcls().getNetworkAcls();

        for (NetworkAcl networkAcl : networkAcls) {
            // Delete all but the default ACL
            if (!networkAcl.getIsDefault()) {
                try {
                    ec2Client.deleteNetworkAcl(new DeleteNetworkAclRequest()
                            .withNetworkAclId(networkAcl.getNetworkAclId()));
                } catch (Exception e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to delete network ACL [%s]: %s", networkAcl.getNetworkAclId(), e.getMessage());
                    LOG.error("Failed to delete network ACL [{}]: {}", networkAcl.getNetworkAclId(), e.getMessage());
                }
            }
        }
    }
}
