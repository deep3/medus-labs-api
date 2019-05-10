package com.deep3.medusLabs.aws.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.organizations.model.Account;
import com.deep3.medusLabs.aws.components.global.S3Component;
import com.deep3.medusLabs.aws.components.regional.CloudFormationComponent;
import com.deep3.medusLabs.aws.components.regional.EC2Component;
import com.deep3.medusLabs.aws.exceptions.NoAvailableServiceComponent;
import com.deep3.medusLabs.aws.model.AWSAccountWrapper;
import com.deep3.medusLabs.aws.monitoring.StackStatusHandler;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.model.socketmessages.SocketMessage;
import com.deep3.medusLabs.model.socketmessages.StackDeploymentMessage;
import com.deep3.medusLabs.service.SocketService;
import com.deep3.medusLabs.service.DeployedLabLogService;
import com.deep3.medusLabs.service.DeployedLabService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tasked with wrapping up all the AWS accounts, the assumed
 * credentials and access to the services contained within.
 *
 */
@Service
public class AccountWranglerService {

    private ArrayList<AWSAccountWrapper> accounts = new ArrayList<>();
    private final OrganisationsService organisationsService;
    private final DeployedLabService deployedLabService;
    private SocketService socketService;
    private DeployedLabLogService deployedLabLogService;

    @Autowired
    public AccountWranglerService(OrganisationsService organisationsService, SocketService socketService, DeployedLabService deployedLabService, DeployedLabLogService deployedLabLogService) {
        this.organisationsService = organisationsService;
        this.socketService = socketService;
        this.deployedLabService = deployedLabService;
        this.deployedLabLogService = deployedLabLogService;
        wrangle(organisationsService.getValidAccounts());
    }

    /**
     * Builds a list of AccountWrapper objects formed from the accounts found in the organisation
     * @param allAccounts The accounts to generate AccountWrappers for
     */
    private void wrangle(List<Account> allAccounts) {
        for(Account account : allAccounts){
            accounts.add(new AWSAccountWrapper(account,organisationsService.getCredentialsForAccount(account)));
        }
    }

    /**
     * Deletes all content from all student accounts. USE WITH CAUTION!
     * @param labID - The Lab ID to use during the removal process
     * @return A HTTPStatus value of OK or EXPECTATION_FAILED
     */
    public HttpStatus wipeAllAccounts(Long labID) {

        try {
            for (AWSAccountWrapper account : accounts) {
                account.wipeAccount(labID);
            }

            return HttpStatus.OK;
        } catch (Exception e) {
            deployedLabLogService.createLog(0, LogLevel.ERROR, "deletion failed for all. ");
            return HttpStatus.EXPECTATION_FAILED;
        }
    }

    /**
     * Deploys a specified cloudFormation stack to the specified region for all accounts.
     * @param region Region to deploy to
     * @param stackName Name of stack
     * @param deployedLab lab to be deployed
     * @throws NoAvailableServiceComponent Specified service not available
     */
    public void deployCloudFormationStack(Regions region, String stackName, AWSAccountWrapper account, DeployedLab deployedLab, List<Parameter> parameters) throws NoAvailableServiceComponent {
        try {
            socketService.sendMessage(new SocketMessage<>(new StackDeploymentMessage(deployedLab.getId(), stackName, account.getAccount().getName(), account.getAccount().getId(), "Started Deployment")));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CloudFormationComponent cloudFormation = account.getRegionalService(CloudFormationComponent.class, region);
        String keyBucketName = createKeyPair(region, stackName, account, deployedLab);
        parameters.add(new Parameter().withParameterKey("S3KeyBucket").withParameterValue(keyBucketName));

        try {
            socketService.sendMessage(new SocketMessage<>(new StackDeploymentMessage(deployedLab.getId(), stackName, account.getAccount().getName(), account.getAccount().getId(), "Created S3 Key + Bucket " + keyBucketName)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            deployedLabLogService.createLog(deployedLab, LogLevel.INFO, "Lab deployment started.");

            cloudFormation.deploy(new CreateStackRequest()
                    .withStackName(stackName)
                    .withTemplateURL(deployedLab.getLab().getTemplateUrl())
                    .withParameters(parameters)
            );

        } catch (Exception e) {
            deployedLabLogService.createLog(deployedLab, LogLevel.ERROR, "Lab creation failed during deployment.");
        }

        cloudFormation.monitorStackCreateStatus(
                new DescribeStacksRequest().withStackName(stackName),
                new StackStatusHandler(stackName, account.getAccount().getId(), socketService, deployedLabService, deployedLab, deployedLabLogService)
        );

    }

    /**
     * Create a key pair called "Ec2AccessKey" in a specified region for a specified member account
     *
     * As part of the process this will create an S3 bucket "private-key-<uuid>" and upload
     * a copy of the private key into it. This is the only place you can retrieve the private key
     * from to access EC2 instances.
     *
     * @param region The AWS Region
     * @param stackName The complete Stack name
     * @param account The related AccountWrapper
     * @param deployedLab The DeployedLab object
     * @return The S3 Bucket name
     * @throws NoAvailableServiceComponent Specified service not available
     */
    private String createKeyPair(Regions region, String stackName, AWSAccountWrapper account, DeployedLab deployedLab) {

        String bucketName = "private-key-" + UUID.randomUUID();
        String keyPairName = "Ec2AccessKey";
        String privateKeyFilename = "access-key-" + region.getName().toLowerCase() + ".pem";

        deployedLabLogService.createLog(deployedLab, LogLevel.INFO,
                "Creating Key Pair for '" + stackName + "'.");

        try {

            S3Component s3 = account.getGlobalService(S3Component.class);
            EC2Component ec2 = account.getRegionalService(EC2Component.class, region);

            String privateKey = ec2.createKeyPair(keyPairName);

            // Save Key to S3 Bucket
            s3.createBucket(bucketName);
            s3.putStringIntoS3File(privateKey, privateKeyFilename, bucketName);
        } catch (NoAvailableServiceComponent message) {
            deployedLabLogService.createLog(deployedLab, LogLevel.ERROR,
                    "AWS event error : " + message);
        }
        return bucketName;
    }

    /**
     * Deletes all cloudFormation stacks for the specified region
     * @param region region to delete stacks in
     * @throws NoAvailableServiceComponent Specified service not available
     */
    public void deleteCloudFormationStacks(Regions region, long deployedLabId) throws NoAvailableServiceComponent {
        for (AWSAccountWrapper account : accounts) {

            CloudFormationComponent cloudFormation = account.getRegionalService(CloudFormationComponent.class, region);
            cloudFormation.deleteAll(deployedLabId);
        }
    }

    /**
     * Get a List of all Accounts
     * @return A List of accounts
     */
    public ArrayList<AWSAccountWrapper> getAccounts() {
        return accounts;
    }
}
