package com.deep3.medusLabs.model.socketmessages;

public class FailedStackDeploymentMessage extends StackDeploymentMessage {
    private String cloudFormationReason;

    public FailedStackDeploymentMessage(Long deployedLabId, String stackName, String accountName, String accountId, String message, String cloudFormationReason) {
        super(deployedLabId, stackName, accountName, accountId, message);
        this.cloudFormationReason = cloudFormationReason;
    }

    public String getCloudFormationReason() {
        return cloudFormationReason;
    }

    public void setCloudFormationReason(String cloudFormationReason) {
        this.cloudFormationReason = cloudFormationReason;
    }
}
