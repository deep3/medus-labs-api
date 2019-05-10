package com.deep3.medusLabs.aws.monitoring;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.waiters.WaiterHandler;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.model.socketmessages.CompletedStackDeploymentMessage;
import com.deep3.medusLabs.model.socketmessages.FailedStackDeploymentMessage;
import com.deep3.medusLabs.model.socketmessages.FinaliseDeploymentMessage;
import com.deep3.medusLabs.model.socketmessages.SocketMessage;
import com.deep3.medusLabs.service.SocketService;
import com.deep3.medusLabs.service.DeployedLabLogService;
import com.deep3.medusLabs.service.DeployedLabService;
import com.deep3.medusLabs.utilities.StackTrackingUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

public class StackStatusHandler extends WaiterHandler {

    private final DeployedLabService deployedLabService;
    private final DeployedLab deployedLab;
    private String stackName;
    private String accountId;
    private SocketService socketService;
    private DeployedLabLogService deployedLabLogService;


    public StackStatusHandler(String stackName,
                              String accountId,
                              SocketService socketService,
                              DeployedLabService deployedLabService,
                              DeployedLab deployedLab,
                              DeployedLabLogService deployedLabLogService
                              ) {

        this.stackName = stackName;
        this.accountId = accountId;
        this.socketService = socketService;
        this.deployedLabService = deployedLabService;
        this.deployedLab = deployedLab;
        this.deployedLabLogService = deployedLabLogService;

    }

    /**
     * Method to be run in the event of successful stack deployment
     * @param request - The AmazonWebServiceRequest object. This parameter is necessary in the original method that this
     *                one overrides, but not used here.
     */
    @Override
    public void onWaitSuccess(AmazonWebServiceRequest request) {
        StackTrackingUtils.increaseNumOfStacksCompleted();

        try {
            socketService.sendMessage(new SocketMessage<>(new CompletedStackDeploymentMessage(deployedLab.getId(), stackName, null, accountId, "Successfully deployed stack")));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        deployedLabLogService.createLog(deployedLab, LogLevel.SUCCESS,
                "Deployment of Lab '" + stackName + "' completed successfully");

        sendEndMessage();
    }

    /**
     * Method to be run in the event of an unsuccessful stack deployment
     * @param exception - the exception met during failure
     */
    @Override
    public void onWaitFailure(Exception exception) {

        FailedStackDeploymentMessage failureMessage = new FailedStackDeploymentMessage(deployedLab.getId(), stackName, null, accountId, "Error deploying lab to account", exception.getMessage());
        StackTrackingUtils.increaseNumOfStacksFailed();

        try {
            socketService.sendMessage(new SocketMessage<>(failureMessage));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        sendEndMessage();
    }

    /**
     * Send a final deployment message via the socket service IF each user has had a successful stack deployment
     */
    private void sendEndMessage() {

        if (StackTrackingUtils.getNumOfUsers() == StackTrackingUtils.getNumOfStacksFinished()) {

            if (StackTrackingUtils.getNumOfFailedStacks() != 0) {
                deployedLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.FAILED);
            } else {
                deployedLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.ACTIVE);
            }
            deployedLabService.saveDeployedLab(deployedLab);
            try {
                socketService.sendMessage(new SocketMessage<>(new FinaliseDeploymentMessage(deployedLab.getId(), "Deployment Completed!")));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            StackTrackingUtils.reset();
        }
    }

}
