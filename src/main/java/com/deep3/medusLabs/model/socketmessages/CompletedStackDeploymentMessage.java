package com.deep3.medusLabs.model.socketmessages;

public class CompletedStackDeploymentMessage extends StackDeploymentMessage {
    public CompletedStackDeploymentMessage(Long deployedLabId, String stackName, String accountName, String accountId, String message) {
        super(deployedLabId, stackName, accountName, accountId, message);
    }
}
