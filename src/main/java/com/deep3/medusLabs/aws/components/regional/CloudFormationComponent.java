package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.*;
import com.amazonaws.waiters.WaiterHandler;
import com.amazonaws.waiters.WaiterParameters;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CloudFormationComponent implements AWSRegionalServiceComponentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CloudFormationComponent.class);

    private final AmazonCloudFormation cloudFormationClient;

    @Autowired
    private DeployedLabLogService deployedLabLogService;

    public CloudFormationComponent(STSAssumeRoleSessionCredentialsProvider credentials, Regions region) throws InvalidAWSCredentials {
        if (credentials == null) {
            throw new InvalidAWSCredentials();
        }
        this.cloudFormationClient = AmazonCloudFormationClientBuilder.standard().withCredentials(credentials).withRegion(region).build();
    }

    /**
     * Deploys CloudFormation stack.
     * @param createStackRequest The configuration for the stack to be deployed
     * @return result of stack deployment
     */
    public CreateStackResult deploy(CreateStackRequest createStackRequest) {
        return cloudFormationClient.createStack(createStackRequest.withCapabilities(Capability.CAPABILITY_NAMED_IAM));
    }

    /**
     * Starts a waiter that monitors a stacks creation status specified in the request and performs actions based on the waiter handler
     * provided.
     * @param request The stack to monitor
     * @param waiterHandler handler to process stack result status
     */
    public void monitorStackCreateStatus(DescribeStacksRequest request, WaiterHandler waiterHandler) {
        cloudFormationClient.waiters().stackCreateComplete().runAsync(new WaiterParameters<>(request), waiterHandler);
    }

    /**
     * Gets a StackEventResult from the stack
     * @param stackName - The Stack name
     * @return - The related DescribeStackEventsResult
     */
    public DescribeStackEventsResult returnStackEvent(String stackName) {
        return cloudFormationClient.describeStackEvents(new DescribeStackEventsRequest().withStackName(stackName));
    }

    @Override
    public void deleteAll(long deployedLabId) {
        ListStacksResult stacks = cloudFormationClient.listStacks(new ListStacksRequest());
        List<StackSummary> stackSummaries = stacks.getStackSummaries();

        while (stacks.getNextToken() != null) {
            stacks = cloudFormationClient.listStacks(new ListStacksRequest()
                    .withNextToken(stacks.getNextToken()));
            stackSummaries.addAll(stacks.getStackSummaries());
        }

        // Loop through deleting stacks by name
        // This method is not robust, does not account for deletion failures e.g. resources left in a bucket will stop a bucket from being deleted
        for (StackSummary stack : stackSummaries) {
            if (!stack.getStackStatus().equals(StackStatus.DELETE_COMPLETE.toString()) &&
            !stack.getStackStatus().equals(StackStatus.DELETE_IN_PROGRESS.toString())) {
                try {
                    cloudFormationClient.deleteStack(new DeleteStackRequest()
                            .withStackName(stack.getStackName()));
                } catch (AmazonCloudFormationException e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to delete Cloud Formation stack [%s]: %s", stack.getStackName(), e.getMessage());
                    LOG.error("Failed to delete Cloud Formation stack [{}]: {}", stack.getStackName(), e.getMessage());
                }
            }
        }
    }
}
