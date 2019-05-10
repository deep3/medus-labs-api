package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.waiters.AmazonEC2Waiters;
import com.amazonaws.waiters.Waiter;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.LogEntry;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EC2Component.class, AmazonEC2ClientBuilder.class})
public class EC2ComponentTest {


    @Mock
    private AmazonEC2Client client;

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    @Mock
    private DeployedLabLogService deployedLabLogService;

    private AmazonEC2ClientBuilder builder;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        builder = PowerMockito.mock(AmazonEC2ClientBuilder.class);
        PowerMockito.mockStatic(AmazonEC2ClientBuilder.class);
        PowerMockito.when(AmazonEC2ClientBuilder.standard()).thenReturn(builder);
        PowerMockito.when(builder.withCredentials(any())).thenReturn(builder);
        PowerMockito.when(builder.withRegion(any(Regions.class))).thenReturn(builder);
        PowerMockito.when(builder.build()).thenReturn(client);
    }

    @Test
    public void testConstructor() throws InvalidAWSCredentials {
        EC2Component test = new EC2Component(credentials, Regions.AP_NORTHEAST_2);
        Assert.assertNotNull("Error creating instance of EC2Component", test);
    }

    @Test(expected = InvalidAWSCredentials.class)
    public void testNullCredentials() throws InvalidAWSCredentials {
        EC2Component ec2Component = new EC2Component(null, Regions.EU_WEST_2);
    }

    @Test
    public void testCreateKeyPair() throws InvalidAWSCredentials {
        CreateKeyPairResult keyPairResult = PowerMockito.mock(CreateKeyPairResult.class);
        PowerMockito.when(client.createKeyPair(any())).thenReturn(keyPairResult);

        KeyPair keyPair = new KeyPair().withKeyMaterial("A");

        PowerMockito.when(keyPairResult.getKeyPair()).thenReturn(keyPair);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        String testResult = ec2Component.createKeyPair("test");

        Assert.assertEquals("Key pair result is not the expected return value","A", testResult);
    }

    @Test
    public void testDeleteAll() throws InvalidAWSCredentials {
        EC2Component ec2Component = Mockito.spy(new EC2Component(credentials, Regions.EU_WEST_2));
        doNothing().when(ec2Component).deleteAllEc2Components(any(Long.class));
        doNothing().when(ec2Component).deleteAllVpcComponents(any(Long.class));

        ec2Component.deleteAll(any(Long.class));

        verify(ec2Component, times(1)).deleteAllEc2Components(any(Long.class));
        verify(ec2Component, times(1)).deleteAllVpcComponents(any(Long.class));
    }

    @Test
    public void testDeleteAllEc2Components() throws InvalidAWSCredentials {
        EC2Component ec2Component = Mockito.spy(new EC2Component(credentials, Regions.EU_WEST_2));
        doNothing().when(ec2Component).deleteInstances(any(Long.class));
        doNothing().when(ec2Component).deleteKeyPairs(any(Long.class));
        doNothing().when(ec2Component).deleteSecurityGroups(any(Long.class));
        doNothing().when(ec2Component).deleteAMIsAndSnapshots(any(Long.class));
        doNothing().when(ec2Component).deleteVolumes(any(Long.class));

        ec2Component.deleteAllEc2Components(any(Long.class));

        verify(ec2Component, times(1)).deleteInstances(any(Long.class));
        verify(ec2Component, times(1)).deleteKeyPairs(any(Long.class));
        verify(ec2Component, times(1)).deleteSecurityGroups(any(Long.class));
        verify(ec2Component, times(1)).deleteAMIsAndSnapshots(any(Long.class));
        verify(ec2Component, times(1)).deleteVolumes(any(Long.class));
    }

    @Test
    public void testDeleteAllVpcComponents() throws InvalidAWSCredentials {
        EC2Component ec2Component = Mockito.spy(new EC2Component(credentials, Regions.EU_WEST_2));
        doNothing().when(ec2Component).deleteNatGateways(any(Long.class));
        doNothing().when(ec2Component).deleteElasticIps(any(Long.class));
        doNothing().when(ec2Component).deleteSubnets(any(Long.class));
        doNothing().when(ec2Component).deleteRouteTables(any(Long.class));
        doNothing().when(ec2Component).deleteNetworkAcls(any(Long.class));
        doNothing().when(ec2Component).deleteInternetGateways(any(Long.class));
        doNothing().when(ec2Component).deleteVPC(any(Long.class));

        ec2Component.deleteAllVpcComponents(any(Long.class));

        verify(ec2Component, times(1)).deleteNatGateways(any(Long.class));
        verify(ec2Component, times(1)).deleteElasticIps(any(Long.class));
        verify(ec2Component, times(1)).deleteRouteTables(any(Long.class));
        verify(ec2Component, times(1)).deleteNetworkAcls(any(Long.class));
        verify(ec2Component, times(1)).deleteInternetGateways(any(Long.class));
        verify(ec2Component, times(1)).deleteVPC(any(Long.class));
    }

    @Test
    public void testDeleteInstances() throws Exception {
        DescribeInstancesResult instancesResult = PowerMockito.mock(DescribeInstancesResult.class);
        PowerMockito.when(client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(instancesResult);

        // Build a realistic set of mock data
        List<Reservation> reservationsA = new ArrayList<>();
        reservationsA.add(new Reservation().withInstances(
                new Instance().withInstanceId("A").withState(new InstanceState().withCode(1))
        ));
        List<Reservation> reservationsB = new ArrayList<>();
        reservationsB.add(new Reservation().withInstances(
                new Instance().withInstanceId("B").withState(new InstanceState().withCode(1))
        ));
        reservationsB.add(new Reservation().withInstances(
                new Instance().withInstanceId("C").withState(new InstanceState().withCode(1))
        ));

        PowerMockito.when(instancesResult.getNextToken()).thenReturn("test").thenReturn(null);
        PowerMockito.when(instancesResult.getReservations()).thenReturn(reservationsA).thenReturn(reservationsB);

        // Instance modification mocking
        ModifyInstanceAttributeRequest modifyInstanceMock = PowerMockito.mock(ModifyInstanceAttributeRequest.class);
        PowerMockito.whenNew(ModifyInstanceAttributeRequest.class).withAnyArguments().thenReturn(modifyInstanceMock);
        PowerMockito.when(modifyInstanceMock.withInstanceId(any())).thenReturn(modifyInstanceMock);

        // Instance termination mocking
        TerminateInstancesRequest terminateInstanceMock = PowerMockito.mock(TerminateInstancesRequest.class);
        PowerMockito.whenNew(TerminateInstancesRequest.class).withAnyArguments().thenReturn(terminateInstanceMock);

        // Mocking waiters
        AmazonEC2Waiters waiters = PowerMockito.mock(AmazonEC2Waiters.class);
        PowerMockito.when(client.waiters()).thenReturn(waiters);
        Waiter waiter = PowerMockito.mock(Waiter.class);
        PowerMockito.when(waiters.instanceTerminated()).thenReturn(waiter);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deleteInstances(any(Long.class));

        List<String> expectedDeletions = Arrays.asList("A", "B", "C");

        // Ensure Termination Protection is off
        verify(modifyInstanceMock, times(3)).withDisableApiTermination(false);
        // Ensure call is made once
        verify(client, times(1)).terminateInstances(any());
        // Ensure instance ID list matches test data
        verify(terminateInstanceMock, times(1)).withInstanceIds(expectedDeletions);
    }

    @Test
    public void testDeleteInstancesExceptions() throws Exception {
        DescribeInstancesResult instancesResult = PowerMockito.mock(DescribeInstancesResult.class);
        PowerMockito.when(client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(instancesResult);

        // Build a realistic set of mock data
        List<Reservation> reservationsA = new ArrayList<>();
        reservationsA.add(new Reservation().withInstances(
                new Instance().withInstanceId("A").withState(new InstanceState().withCode(1))
        ));
        List<Reservation> reservationsB = new ArrayList<>();
        reservationsB.add(new Reservation().withInstances(
                new Instance().withInstanceId("B").withState(new InstanceState().withCode(1))
        ));
        reservationsB.add(new Reservation().withInstances(
                new Instance().withInstanceId("C").withState(new InstanceState().withCode(1))
        ));

        PowerMockito.when(instancesResult.getNextToken()).thenReturn("test").thenReturn(null);
        PowerMockito.when(instancesResult.getReservations()).thenReturn(reservationsA).thenReturn(reservationsB);

        // Instance modification mocking
        ModifyInstanceAttributeRequest modifyInstanceMock = PowerMockito.mock(ModifyInstanceAttributeRequest.class);
        PowerMockito.whenNew(ModifyInstanceAttributeRequest.class).withAnyArguments().thenReturn(modifyInstanceMock);
        PowerMockito.when(modifyInstanceMock.withInstanceId(any())).thenReturn(modifyInstanceMock);
        PowerMockito.when(client.modifyInstanceAttribute(any())).thenThrow(AmazonEC2Exception.class);

        // Instance termination mocking
        TerminateInstancesRequest terminateInstanceMock = PowerMockito.mock(TerminateInstancesRequest.class);
        PowerMockito.whenNew(TerminateInstancesRequest.class).withAnyArguments().thenReturn(terminateInstanceMock);
        PowerMockito.when(client.terminateInstances(any())).thenThrow(AmazonEC2Exception.class);

        // Mocking waiters
        AmazonEC2Waiters waiters = PowerMockito.mock(AmazonEC2Waiters.class);
        PowerMockito.when(client.waiters()).thenReturn(waiters);
        Waiter waiter = PowerMockito.mock(Waiter.class);
        PowerMockito.when(waiters.instanceTerminated()).thenReturn(waiter);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteInstances(1L);

        // Verify all calls are attempted despite exceptions
        verify(client, times(3)).modifyInstanceAttribute(any());
        verify(client, times(1)).terminateInstances(any());
    }

    @Test
    public void testDeleteKeyPairs() throws Exception {
        DescribeKeyPairsResult keyPairsResult = PowerMockito.mock(DescribeKeyPairsResult.class);
        PowerMockito.when(client.describeKeyPairs()).thenReturn(keyPairsResult);

        List<KeyPairInfo> keyPairInfoList = new ArrayList<>();
        keyPairInfoList.add(new KeyPairInfo().withKeyName("A").withKeyFingerprint("A"));
        keyPairInfoList.add(new KeyPairInfo().withKeyName("B").withKeyFingerprint("B"));

        PowerMockito.when(keyPairsResult.getKeyPairs()).thenReturn(keyPairInfoList);

        DeleteKeyPairRequest deleteKeyPairMock = PowerMockito.mock(DeleteKeyPairRequest.class);
        PowerMockito.whenNew(DeleteKeyPairRequest.class).withAnyArguments().thenReturn(deleteKeyPairMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deleteKeyPairs(1L);

        List<String> expectedDeletions = Arrays.asList("A", "B");

        verify(client, times(2)).deleteKeyPair(any());
        verify(deleteKeyPairMock, times(2)).withKeyName(argThat(deleting -> expectedDeletions.contains(deleting)));
    }

    @Test
    public void testDeleteKeyPairsException() throws Exception {
        DescribeKeyPairsResult keyPairsResult = PowerMockito.mock(DescribeKeyPairsResult.class);
        PowerMockito.when(client.describeKeyPairs()).thenReturn(keyPairsResult);

        List<KeyPairInfo> keyPairInfoList = new ArrayList<>();
        keyPairInfoList.add(new KeyPairInfo().withKeyName("A").withKeyFingerprint("A"));
        keyPairInfoList.add(new KeyPairInfo().withKeyName("B").withKeyFingerprint("B"));

        PowerMockito.when(keyPairsResult.getKeyPairs()).thenReturn(keyPairInfoList);

        DeleteKeyPairRequest deleteKeyPairMock = PowerMockito.mock(DeleteKeyPairRequest.class);
        PowerMockito.whenNew(DeleteKeyPairRequest.class).withAnyArguments().thenReturn(deleteKeyPairMock);
        PowerMockito.when(client.deleteKeyPair(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteKeyPairs(1L);

        verify(client, times(2)).deleteKeyPair(any());
    }

    @Test
    public void testDeleteSecurityGroups() throws Exception {
        DescribeSecurityGroupsResult securityGroupsResult = PowerMockito.mock(DescribeSecurityGroupsResult.class);
        PowerMockito.when(client.describeSecurityGroups()).thenReturn(securityGroupsResult);
        PowerMockito.when(client.describeSecurityGroups(any(DescribeSecurityGroupsRequest.class))).thenReturn(securityGroupsResult);

        // Build a realistic set of mock data
        List<SecurityGroup> securityGroupsA = new ArrayList<>();
        securityGroupsA.add(new SecurityGroup().withGroupName("default").withGroupId("default").withIpPermissions(new IpPermission()).withIpPermissionsEgress(new IpPermission()));
        securityGroupsA.add(new SecurityGroup().withGroupName("A").withGroupId("A"));
        List<SecurityGroup> securityGroupsB = new ArrayList<>();
        securityGroupsB.add(new SecurityGroup().withGroupName("B").withGroupId("B"));

        PowerMockito.when(securityGroupsResult.getNextToken()).thenReturn("test").thenReturn(null);
        PowerMockito.when(securityGroupsResult.getSecurityGroups()).thenReturn(securityGroupsA).thenReturn(securityGroupsB);

        // Ingress Mocking
        RevokeSecurityGroupIngressRequest ingressRequest = PowerMockito.mock(RevokeSecurityGroupIngressRequest.class);
        PowerMockito.whenNew(RevokeSecurityGroupIngressRequest.class).withAnyArguments().thenReturn(ingressRequest);
        PowerMockito.when(ingressRequest.withGroupId(any())).thenReturn(ingressRequest);

        // Egress Mocking
        RevokeSecurityGroupEgressRequest egressRequest = PowerMockito.mock(RevokeSecurityGroupEgressRequest.class);
        PowerMockito.whenNew(RevokeSecurityGroupEgressRequest.class).withAnyArguments().thenReturn(egressRequest);
        PowerMockito.when(egressRequest.withGroupId(any())).thenReturn(egressRequest);

        // Deletion Mocking
        DeleteSecurityGroupRequest deleteSecurityGroupMock = PowerMockito.mock(DeleteSecurityGroupRequest.class);
        PowerMockito.whenNew(DeleteSecurityGroupRequest.class).withAnyArguments().thenReturn(deleteSecurityGroupMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deleteSecurityGroups(1L);

        List<String> expectedDeletions = Arrays.asList("A", "B");

        // Verify ingress removal on the default group
        verify(client, times(1)).revokeSecurityGroupIngress(any());
        verify(ingressRequest, times(1)).withGroupId("default");

        // Verify egress removal on the default group
        verify(client, times(1)).revokeSecurityGroupEgress(any());
        verify(ingressRequest, times(1)).withGroupId("default");

        // Verify deletion of all other groups
        verify(client, times(2)).deleteSecurityGroup(any());
        verify(deleteSecurityGroupMock, times(2)).withGroupId(argThat(deleting -> expectedDeletions.contains(deleting)));
    }

    @Test
    public void testDeleteSecurityGroupsExceptions() throws Exception {
        DescribeSecurityGroupsResult securityGroupsResult = PowerMockito.mock(DescribeSecurityGroupsResult.class);
        PowerMockito.when(client.describeSecurityGroups()).thenReturn(securityGroupsResult);
        PowerMockito.when(client.describeSecurityGroups(any(DescribeSecurityGroupsRequest.class))).thenReturn(securityGroupsResult);

        // Build a realistic set of mock data
        List<SecurityGroup> securityGroupsA = new ArrayList<>();
        securityGroupsA.add(new SecurityGroup().withGroupName("default").withGroupId("default").withIpPermissions(new IpPermission()).withIpPermissionsEgress(new IpPermission()));
        securityGroupsA.add(new SecurityGroup().withGroupName("A").withGroupId("A"));
        List<SecurityGroup> securityGroupsB = new ArrayList<>();
        securityGroupsB.add(new SecurityGroup().withGroupName("B").withGroupId("B"));

        PowerMockito.when(securityGroupsResult.getNextToken()).thenReturn("test").thenReturn(null);
        PowerMockito.when(securityGroupsResult.getSecurityGroups()).thenReturn(securityGroupsA).thenReturn(securityGroupsB);

        // Ingress Mocking
        RevokeSecurityGroupIngressRequest ingressRequest = PowerMockito.mock(RevokeSecurityGroupIngressRequest.class);
        PowerMockito.whenNew(RevokeSecurityGroupIngressRequest.class).withAnyArguments().thenReturn(ingressRequest);
        PowerMockito.when(ingressRequest.withGroupId(any())).thenReturn(ingressRequest);
        PowerMockito.when(client.revokeSecurityGroupIngress(any())).thenThrow(AmazonEC2Exception.class);

        // Egress Mocking
        RevokeSecurityGroupEgressRequest egressRequest = PowerMockito.mock(RevokeSecurityGroupEgressRequest.class);
        PowerMockito.whenNew(RevokeSecurityGroupEgressRequest.class).withAnyArguments().thenReturn(egressRequest);
        PowerMockito.when(egressRequest.withGroupId(any())).thenReturn(egressRequest);
        PowerMockito.when(client.revokeSecurityGroupEgress(any())).thenThrow(AmazonEC2Exception.class);

        // Deletion Mocking
        DeleteSecurityGroupRequest deleteSecurityGroupMock = PowerMockito.mock(DeleteSecurityGroupRequest.class);
        PowerMockito.whenNew(DeleteSecurityGroupRequest.class).withAnyArguments().thenReturn(deleteSecurityGroupMock);
        PowerMockito.when(client.deleteSecurityGroup(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteSecurityGroups(1L);

        verify(client, times(1)).revokeSecurityGroupIngress(any());
        verify(client, times(1)).revokeSecurityGroupEgress(any());
        verify(client, times(2)).deleteSecurityGroup(any());
    }

    @Test
    public void testDeleteAMIsAndSnapshots() throws Exception {
        DescribeImagesResult imagesResult = PowerMockito.mock(DescribeImagesResult.class);
        PowerMockito.when(client.describeImages(any(DescribeImagesRequest.class))).thenReturn(imagesResult);

        // Build a realistic set of AMI mock data
        List<Image> images = Arrays.asList(
                new Image().withImageId("A"),
                new Image().withImageId("B"),
                new Image().withImageId("C")
        );

        PowerMockito.when(imagesResult.getImages()).thenReturn(images);

        // Deregister AMI mocking
        DeregisterImageRequest deregisterImageMock = PowerMockito.mock(DeregisterImageRequest.class);
        PowerMockito.whenNew(DeregisterImageRequest.class).withAnyArguments().thenReturn(deregisterImageMock);

        DescribeSnapshotsResult snapshotsResult = PowerMockito.mock(DescribeSnapshotsResult.class);
        PowerMockito.when(client.describeSnapshots(any(DescribeSnapshotsRequest.class))).thenReturn(snapshotsResult);

        // Build a realistic set of snapshot mock data
        List<Snapshot> snapshotsA = new ArrayList<>();
        snapshotsA.add(new Snapshot().withSnapshotId("D"));
        snapshotsA.add(new Snapshot().withSnapshotId("E"));
        List<Snapshot> snapshotsB = new ArrayList<>();
        snapshotsB.add(new Snapshot().withSnapshotId("F"));

        PowerMockito.when(snapshotsResult.getNextToken()).thenReturn("test").thenReturn(null);
        PowerMockito.when(snapshotsResult.getSnapshots()).thenReturn(snapshotsA).thenReturn(snapshotsB);

        // Delete snapshot mocking
        DeleteSnapshotRequest deleteSnapshotMock = PowerMockito.mock(DeleteSnapshotRequest.class);
        PowerMockito.whenNew(DeleteSnapshotRequest.class).withAnyArguments().thenReturn(deleteSnapshotMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deleteAMIsAndSnapshots(1L);

        List<String> expectedImages = Arrays.asList("A", "B", "C");
        List<String> expectedSnapshots = Arrays.asList("D", "E", "F");

        // Verify AMI deregistration
        verify(client, times(3)).deregisterImage(any());
        verify(deregisterImageMock, times(3)).withImageId(argThat(image -> expectedImages.contains(image)));

        // Verify snapshot deletion
        verify(client, times(3)).deleteSnapshot(any());
        verify(deleteSnapshotMock, times(3)).withSnapshotId(argThat(snapshot -> expectedSnapshots.contains(snapshot)));
    }

    @Test
    public void testDeleteAMIsAndSnapshotsExceptions() throws Exception {
        DescribeImagesResult imagesResult = PowerMockito.mock(DescribeImagesResult.class);
        PowerMockito.when(client.describeImages(any(DescribeImagesRequest.class))).thenReturn(imagesResult);

        // Build a realistic set of AMI mock data
        List<Image> images = Arrays.asList(
                new Image().withImageId("A"),
                new Image().withImageId("B"),
                new Image().withImageId("C")
        );

        PowerMockito.when(imagesResult.getImages()).thenReturn(images);

        // Deregister AMI mocking
        DeregisterImageRequest deregisterImageMock = PowerMockito.mock(DeregisterImageRequest.class);
        PowerMockito.whenNew(DeregisterImageRequest.class).withAnyArguments().thenReturn(deregisterImageMock);
        PowerMockito.when(client.deregisterImage(any())).thenThrow(AmazonEC2Exception.class);

        DescribeSnapshotsResult snapshotsResult = PowerMockito.mock(DescribeSnapshotsResult.class);
        PowerMockito.when(client.describeSnapshots(any(DescribeSnapshotsRequest.class))).thenReturn(snapshotsResult);

        // Build a realistic set of snapshot mock data
        List<Snapshot> snapshotsA = new ArrayList<>();
        snapshotsA.add(new Snapshot().withSnapshotId("D"));
        snapshotsA.add(new Snapshot().withSnapshotId("E"));
        List<Snapshot> snapshotsB = new ArrayList<>();
        snapshotsB.add(new Snapshot().withSnapshotId("F"));

        PowerMockito.when(snapshotsResult.getNextToken()).thenReturn("test").thenReturn(null);
        PowerMockito.when(snapshotsResult.getSnapshots()).thenReturn(snapshotsA).thenReturn(snapshotsB);

        // Delete snapshot mocking
        DeleteSnapshotRequest deleteSnapshotMock = PowerMockito.mock(DeleteSnapshotRequest.class);
        PowerMockito.whenNew(DeleteSnapshotRequest.class).withAnyArguments().thenReturn(deleteSnapshotMock);
        PowerMockito.when(client.deleteSnapshot(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteAMIsAndSnapshots(1L);

        verify(client, times(3)).deregisterImage(any());
        verify(client, times(3)).deleteSnapshot(any());
    }

    @Test
    public void testDeleteVolumes() throws Exception {
        DescribeVolumesResult volumesResult = PowerMockito.mock(DescribeVolumesResult.class);
        PowerMockito.when(client.describeVolumes()).thenReturn(volumesResult);
        PowerMockito.when(client.describeVolumes(any(DescribeVolumesRequest.class))).thenReturn(volumesResult);

        // Build a realistic set of data
        List<Volume> volumesA = new ArrayList<>();
        volumesA.add(new Volume().withVolumeId("A"));
        volumesA.add(new Volume().withVolumeId("B"));
        List<Volume> volumesB = new ArrayList<>();
        volumesB.add(new Volume().withVolumeId("C"));

        PowerMockito.when(volumesResult.getNextToken()).thenReturn("test").thenReturn(null);
        PowerMockito.when(volumesResult.getVolumes()).thenReturn(volumesA).thenReturn(volumesB);

        // Delete volumes mocking
        DeleteVolumeRequest deleteVolumeMock = PowerMockito.mock(DeleteVolumeRequest.class);
        PowerMockito.whenNew(DeleteVolumeRequest.class).withAnyArguments().thenReturn(deleteVolumeMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deleteVolumes(1L);

        List<String> expectedResults = Arrays.asList("A", "B", "C");

        // Verify deletion of correct volumes
        verify(client, times(3)).deleteVolume(any());
        verify(deleteVolumeMock, times(3)).withVolumeId(argThat(volumeId -> expectedResults.contains(volumeId)));
    }

    @Test
    public void testDeleteVolumesExceptions() throws Exception {
        DescribeVolumesResult volumesResult = PowerMockito.mock(DescribeVolumesResult.class);
        PowerMockito.when(client.describeVolumes()).thenReturn(volumesResult);
        PowerMockito.when(client.describeVolumes(any(DescribeVolumesRequest.class))).thenReturn(volumesResult);

        // Build a realistic set of data
        List<Volume> volumesA = new ArrayList<>();
        volumesA.add(new Volume().withVolumeId("A"));
        volumesA.add(new Volume().withVolumeId("B"));
        List<Volume> volumesB = new ArrayList<>();
        volumesB.add(new Volume().withVolumeId("C"));

        PowerMockito.when(volumesResult.getNextToken()).thenReturn("test").thenReturn(null);
        PowerMockito.when(volumesResult.getVolumes()).thenReturn(volumesA).thenReturn(volumesB);

        // Delete volumes mocking
        DeleteVolumeRequest deleteVolumeMock = PowerMockito.mock(DeleteVolumeRequest.class);
        PowerMockito.whenNew(DeleteVolumeRequest.class).withAnyArguments().thenReturn(deleteVolumeMock);
        PowerMockito.when(client.deleteVolume(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteVolumes(1L);

        verify(client, times(3)).deleteVolume(any());
    }

    @Test
    public void testDeleteElasticIps() throws Exception {
        DescribeAddressesResult addressesResult = PowerMockito.mock(DescribeAddressesResult.class);
        PowerMockito.when(client.describeAddresses()).thenReturn(addressesResult);

        // Build a realistic set of data
        List<Address> addresses = Arrays.asList(
                new Address().withAllocationId("A"),
                new Address().withAllocationId("B"),
                new Address().withAllocationId("C")
        );

        PowerMockito.when(addressesResult.getAddresses()).thenReturn(addresses);

        // Delete addresses mocking
        ReleaseAddressRequest releaseAddressMock = PowerMockito.mock(ReleaseAddressRequest.class);
        PowerMockito.whenNew(ReleaseAddressRequest.class).withAnyArguments().thenReturn(releaseAddressMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deleteElasticIps(1L);

        List<String> expectedResults = Arrays.asList("A", "B", "C");

        // Verify release of correct IPs
        verify(client, times(3)).releaseAddress(any());
        verify(releaseAddressMock, times(3)).withAllocationId(argThat(id -> expectedResults.contains(id)));
    }

    @Test
    public void testDeleteElasticIpsExceptions() throws Exception {
        DescribeAddressesResult addressesResult = PowerMockito.mock(DescribeAddressesResult.class);
        PowerMockito.when(client.describeAddresses()).thenReturn(addressesResult);

        // Build a realistic set of data
        List<Address> addresses = Arrays.asList(
                new Address().withAllocationId("A"),
                new Address().withAllocationId("B"),
                new Address().withAllocationId("C")
        );

        PowerMockito.when(addressesResult.getAddresses()).thenReturn(addresses);

        // Delete addresses mocking
        ReleaseAddressRequest releaseAddressMock = PowerMockito.mock(ReleaseAddressRequest.class);
        PowerMockito.whenNew(ReleaseAddressRequest.class).withAnyArguments().thenReturn(releaseAddressMock);
        PowerMockito.when(client.releaseAddress(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteElasticIps(1L);

        verify(client, times(3)).releaseAddress(any());
    }

    @Test
    public void testDeleteNatGateways() throws Exception {
        DescribeNatGatewaysResult natGatewaysResult = PowerMockito.mock(DescribeNatGatewaysResult.class);
        PowerMockito.when(client.describeNatGateways(any(DescribeNatGatewaysRequest.class))).thenReturn(natGatewaysResult);

        List<NatGateway> natGateways = Arrays.asList(
                new NatGateway().withNatGatewayId("A").withState("deleted"),
                new NatGateway().withNatGatewayId("B").withState("available"),
                new NatGateway().withNatGatewayId("C").withState("available")
        );

        List<NatGateway> deletedNatGateways = Arrays.asList(
                new NatGateway().withNatGatewayId("A").withState("deleted"),
                new NatGateway().withNatGatewayId("B").withState("deleted"),
                new NatGateway().withNatGatewayId("C").withState("deleted")
        );

        // First return initial data, second return data to keep waiting loop active, third to end the waiting loop
        PowerMockito.when(natGatewaysResult.getNatGateways()).thenReturn(natGateways).thenReturn(natGateways).thenReturn(deletedNatGateways);

        // Delete NAT gateway mocking
        DeleteNatGatewayRequest deleteNatGatewayMock = PowerMockito.mock(DeleteNatGatewayRequest.class);
        PowerMockito.whenNew(DeleteNatGatewayRequest.class).withAnyArguments().thenReturn(deleteNatGatewayMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deleteNatGateways(1L);

        List<String> expectedResults = Arrays.asList("B", "C");

        verify(client, times(2)).deleteNatGateway(any());
        verify(deleteNatGatewayMock, times(2)).withNatGatewayId(argThat(id -> expectedResults.contains(id)));
    }

    @Test
    public void testDeleteNatGatewaysExceptions() throws Exception {
        DescribeNatGatewaysResult natGatewaysResult = PowerMockito.mock(DescribeNatGatewaysResult.class);
        PowerMockito.when(client.describeNatGateways(any(DescribeNatGatewaysRequest.class))).thenReturn(natGatewaysResult);

        List<NatGateway> natGateways = Arrays.asList(
                new NatGateway().withNatGatewayId("A").withState("deleted"),
                new NatGateway().withNatGatewayId("B").withState("available"),
                new NatGateway().withNatGatewayId("C").withState("available")
        );

        // First return initial data, second return data to keep waiting loop active, third to end the waiting loop
        PowerMockito.when(natGatewaysResult.getNatGateways()).thenReturn(natGateways).thenReturn(natGateways);

        // Delete NAT gateway mocking
        DeleteNatGatewayRequest deleteNatGatewayMock = PowerMockito.mock(DeleteNatGatewayRequest.class);
        PowerMockito.whenNew(DeleteNatGatewayRequest.class).withAnyArguments().thenReturn(deleteNatGatewayMock);
        PowerMockito.when(client.deleteNatGateway(any())).thenThrow(AmazonEC2Exception.class);

        // Mock interrupted exception
        CountDownLatch countDownLatch = PowerMockito.mock(CountDownLatch.class);
        PowerMockito.whenNew(CountDownLatch.class).withAnyArguments().thenReturn(countDownLatch);
        PowerMockito.doThrow(new InterruptedException()).when(countDownLatch).await(anyLong(), any(TimeUnit.class));

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteNatGateways(1L);

        verify(client, times(2)).deleteNatGateway(any());
    }

    @Test
    public void testDeleteVPC() throws Exception {
        DescribeVpcsResult vpcsResult = PowerMockito.mock(DescribeVpcsResult.class);
        PowerMockito.when(client.describeVpcs()).thenReturn(vpcsResult);

        List<Vpc> vpcs = Arrays.asList(
                new Vpc().withVpcId("A"),
                new Vpc().withVpcId("B"),
                new Vpc().withVpcId("C")
        );

        PowerMockito.when(vpcsResult.getVpcs()).thenReturn(vpcs);

        // Delete VPC mocking
        DeleteVpcRequest deleteVpcMock = PowerMockito.mock(DeleteVpcRequest.class);
        PowerMockito.whenNew(DeleteVpcRequest.class).withAnyArguments().thenReturn(deleteVpcMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deleteVPC(1L);

        List<String> expectedResults = Arrays.asList("A", "B", "C");

        // Verify correct VPCs are deleted
        verify(client, times(3)).deleteVpc(any());
        verify(deleteVpcMock, times(3)).withVpcId(argThat(vpcId -> expectedResults.contains(vpcId)));
    }

    @Test
    public void testDeleteVPCException() throws Exception {
        DescribeVpcsResult vpcsResult = PowerMockito.mock(DescribeVpcsResult.class);
        PowerMockito.when(client.describeVpcs()).thenReturn(vpcsResult);

        List<Vpc> vpcs = Arrays.asList(
                new Vpc().withVpcId("A"),
                new Vpc().withVpcId("B"),
                new Vpc().withVpcId("C")
        );

        PowerMockito.when(vpcsResult.getVpcs()).thenReturn(vpcs);

        // Delete VPC mocking
        DeleteVpcRequest deleteVpcMock = PowerMockito.mock(DeleteVpcRequest.class);
        PowerMockito.whenNew(DeleteVpcRequest.class).withAnyArguments().thenReturn(deleteVpcMock);
        PowerMockito.when(client.deleteVpc(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteVPC(1L);

        verify(client, times(3)).deleteVpc(any());
    }

    @Test
    public void testDeleteSubnets() throws Exception {
        DescribeSubnetsResult subnetsResult = PowerMockito.mock(DescribeSubnetsResult.class);
        PowerMockito.when(client.describeSubnets()).thenReturn(subnetsResult);

        List<Subnet> subnets = Arrays.asList(
                new Subnet().withSubnetId("A"),
                new Subnet().withSubnetId("B"),
                new Subnet().withSubnetId("C")
        );

        PowerMockito.when(subnetsResult.getSubnets()).thenReturn(subnets);

        //Delete subnet mocking
        DeleteSubnetRequest deleteSubnetMock = PowerMockito.mock(DeleteSubnetRequest.class);
        PowerMockito.whenNew(DeleteSubnetRequest.class).withAnyArguments().thenReturn(deleteSubnetMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteSubnets(1L);

        List<String> expectedResults = Arrays.asList("A", "B", "C");

        // Verify correct subnets are deleted
        verify(deleteSubnetMock, times(3)).withSubnetId(argThat(subnetId -> expectedResults.contains(subnetId)));
    }

    @Test
    public void testDeleteSubnetsExceptions() throws Exception {
        DescribeSubnetsResult subnetsResult = PowerMockito.mock(DescribeSubnetsResult.class);
        PowerMockito.when(client.describeSubnets()).thenReturn(subnetsResult);

        List<Subnet> subnets = Arrays.asList(
                new Subnet().withSubnetId("A"),
                new Subnet().withSubnetId("B"),
                new Subnet().withSubnetId("C")
        );

        PowerMockito.when(subnetsResult.getSubnets()).thenReturn(subnets);

        //Delete subnet mocking
        DeleteSubnetRequest deleteSubnetMock = PowerMockito.mock(DeleteSubnetRequest.class);
        PowerMockito.whenNew(DeleteSubnetRequest.class).withAnyArguments().thenReturn(deleteSubnetMock);
        PowerMockito.when(client.deleteSubnet(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteSubnets(1L);

        verify(client, times(3)).deleteSubnet(any());
    }

    @Test
    public void testDeleteRouteTables() throws Exception {
        DescribeRouteTablesResult routeTablesResult = PowerMockito.mock(DescribeRouteTablesResult.class);
        PowerMockito.when(client.describeRouteTables()).thenReturn(routeTablesResult);

        List<RouteTable> routeTables = Arrays.asList(
                new RouteTable().withRouteTableId("A"),
                new RouteTable().withRouteTableId("B"),
                new RouteTable().withRouteTableId("C")
        );

        PowerMockito.when(routeTablesResult.getRouteTables()).thenReturn(routeTables);

        //Delete route table mocking
        DeleteRouteTableRequest deleteRouteTableMock = PowerMockito.mock(DeleteRouteTableRequest.class);
        PowerMockito.whenNew(DeleteRouteTableRequest.class).withAnyArguments().thenReturn(deleteRouteTableMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteRouteTables(1L);

        List<String> expectedResults = Arrays.asList("A", "B", "C");

        // Verify correct route tables are deleted
        verify(client, times(3)).deleteRouteTable(any());
        verify(deleteRouteTableMock, times(3)).withRouteTableId(argThat(subnetId -> expectedResults.contains(subnetId)));
    }

    @Test
    public void testDeleteRouteTablesExceptions() throws Exception {
        DescribeRouteTablesResult routeTablesResult = PowerMockito.mock(DescribeRouteTablesResult.class);
        PowerMockito.when(client.describeRouteTables()).thenReturn(routeTablesResult);

        List<RouteTable> routeTables = Arrays.asList(
                new RouteTable().withRouteTableId("A"),
                new RouteTable().withRouteTableId("B"),
                new RouteTable().withRouteTableId("C")
        );

        PowerMockito.when(routeTablesResult.getRouteTables()).thenReturn(routeTables);

        // Delete route table mocking
        DeleteRouteTableRequest deleteRouteTableMock = PowerMockito.mock(DeleteRouteTableRequest.class);
        PowerMockito.whenNew(DeleteRouteTableRequest.class).withAnyArguments().thenReturn(deleteRouteTableMock);
        PowerMockito.when(client.deleteRouteTable(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteRouteTables(1L);

        verify(client, times(3)).deleteRouteTable(any());
    }

    @Test
    public void testDeleteInternetGateways() throws Exception {
        DescribeInternetGatewaysResult internetGatewaysResult = PowerMockito.mock(DescribeInternetGatewaysResult.class);
        PowerMockito.when(client.describeInternetGateways()).thenReturn(internetGatewaysResult);

        List<InternetGateway> internetGateways = Arrays.asList(
                new InternetGateway().withInternetGatewayId("A").withAttachments(new InternetGatewayAttachment().withVpcId("D")),
                new InternetGateway().withInternetGatewayId("B").withAttachments(new InternetGatewayAttachment().withVpcId("E")),
                new InternetGateway().withInternetGatewayId("C").withAttachments(new InternetGatewayAttachment().withVpcId("F"))
        );

        PowerMockito.when(internetGatewaysResult.getInternetGateways()).thenReturn(internetGateways);

        // Detach internet gateway from VPC mocking
        DetachInternetGatewayRequest detachInternetGatewayMock = PowerMockito.mock(DetachInternetGatewayRequest.class);
        PowerMockito.whenNew(DetachInternetGatewayRequest.class).withAnyArguments().thenReturn(detachInternetGatewayMock);
        PowerMockito.when(detachInternetGatewayMock.withInternetGatewayId(any())).thenReturn(detachInternetGatewayMock);

        // Delete internet gateway mocking
        DeleteInternetGatewayRequest deleteInternetGatewayMock = PowerMockito.mock(DeleteInternetGatewayRequest.class);
        PowerMockito.whenNew(DeleteInternetGatewayRequest.class).withAnyArguments().thenReturn(deleteInternetGatewayMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteInternetGateways(1L);

        List<String> expectedGatewayIds = Arrays.asList("A", "B", "C");
        List<String> expectedVpcIds = Arrays.asList("D", "E", "F");

        // Verify gateway detachment
        verify(client, times(3)).detachInternetGateway(any());
        verify(detachInternetGatewayMock, times(3)).withInternetGatewayId(argThat(id -> expectedGatewayIds.contains(id)));
        verify(detachInternetGatewayMock, times(3)).withVpcId(argThat(id -> expectedVpcIds.contains(id)));

        // Verify gateway deletion
        verify(client, times(3)).deleteInternetGateway(any());
        verify(deleteInternetGatewayMock, times(3)).withInternetGatewayId(argThat(id -> expectedGatewayIds.contains(id)));
    }

    @Test
    public void testDeleteInternetGatewaysExceptions() throws Exception {
        DescribeInternetGatewaysResult internetGatewaysResult = PowerMockito.mock(DescribeInternetGatewaysResult.class);
        PowerMockito.when(client.describeInternetGateways()).thenReturn(internetGatewaysResult);

        List<InternetGateway> internetGateways = Arrays.asList(
                new InternetGateway().withInternetGatewayId("A").withAttachments(new InternetGatewayAttachment().withVpcId("D")),
                new InternetGateway().withInternetGatewayId("B").withAttachments(new InternetGatewayAttachment().withVpcId("E")),
                new InternetGateway().withInternetGatewayId("C").withAttachments(new InternetGatewayAttachment().withVpcId("F"))
        );

        PowerMockito.when(internetGatewaysResult.getInternetGateways()).thenReturn(internetGateways);

        // Detach internet gateway from VPC mocking
        DetachInternetGatewayRequest detachInternetGatewayMock = PowerMockito.mock(DetachInternetGatewayRequest.class);
        PowerMockito.whenNew(DetachInternetGatewayRequest.class).withAnyArguments().thenReturn(detachInternetGatewayMock);
        PowerMockito.when(detachInternetGatewayMock.withInternetGatewayId(any())).thenReturn(detachInternetGatewayMock);
        PowerMockito.when(client.detachInternetGateway(any())).thenThrow(AmazonEC2Exception.class);

        // Delete internet gateway mocking
        DeleteInternetGatewayRequest deleteInternetGatewayMock = PowerMockito.mock(DeleteInternetGatewayRequest.class);
        PowerMockito.whenNew(DeleteInternetGatewayRequest.class).withAnyArguments().thenReturn(deleteInternetGatewayMock);
        PowerMockito.when(client.deleteInternetGateway(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));
        ec2Component.deleteInternetGateways(1L);

        verify(client, times(3)).detachInternetGateway(any());
        verify(client, times(3)).deleteInternetGateway(any());
    }

    @Test
    public void testDeleteNetworkAcls() throws Exception {
        DescribeNetworkAclsResult networkAclsResult = PowerMockito.mock(DescribeNetworkAclsResult.class);
        PowerMockito.when(client.describeNetworkAcls()).thenReturn(networkAclsResult);

        List<NetworkAcl> networkAcls = Arrays.asList(
                new NetworkAcl().withIsDefault(true).withNetworkAclId("A"),
                new NetworkAcl().withIsDefault(false).withNetworkAclId("B"),
                new NetworkAcl().withIsDefault(false).withNetworkAclId("C")
        );

        PowerMockito.when(networkAclsResult.getNetworkAcls()).thenReturn(networkAcls);

        // Delete network ACL mocking
        DeleteNetworkAclRequest deleteNetworkAclMock = PowerMockito.mock(DeleteNetworkAclRequest.class);
        PowerMockito.whenNew(DeleteNetworkAclRequest.class).withAnyArguments().thenReturn(deleteNetworkAclMock);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteNetworkAcls(1L);

        // 1 item less than input as the default ACLs are not deleted
        List<String> expectedResults = Arrays.asList("B", "C");

        // Verify network ACL deletion
        verify(client, times(2)).deleteNetworkAcl(any());
        verify(deleteNetworkAclMock, times(2)).withNetworkAclId(argThat(id -> expectedResults.contains(id)));
    }

    @Test
    public void testDeleteNetworkAclsExceptions() throws Exception {
        DescribeNetworkAclsResult networkAclsResult = PowerMockito.mock(DescribeNetworkAclsResult.class);
        PowerMockito.when(client.describeNetworkAcls()).thenReturn(networkAclsResult);

        List<NetworkAcl> networkAcls = Arrays.asList(
                new NetworkAcl().withIsDefault(true).withNetworkAclId("A"),
                new NetworkAcl().withIsDefault(false).withNetworkAclId("B"),
                new NetworkAcl().withIsDefault(false).withNetworkAclId("C")
        );

        PowerMockito.when(networkAclsResult.getNetworkAcls()).thenReturn(networkAcls);

        // Delete network ACL mocking
        DeleteNetworkAclRequest deleteNetworkAclMock = PowerMockito.mock(DeleteNetworkAclRequest.class);
        PowerMockito.whenNew(DeleteNetworkAclRequest.class).withAnyArguments().thenReturn(deleteNetworkAclMock);
        PowerMockito.when(client.deleteNetworkAcl(any())).thenThrow(AmazonEC2Exception.class);

        EC2Component ec2Component = new EC2Component(credentials, Regions.EU_WEST_2);
        ec2Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        ec2Component.deleteNetworkAcls(1L);

        verify(client, times(2)).deleteNetworkAcl(any());
    }
}