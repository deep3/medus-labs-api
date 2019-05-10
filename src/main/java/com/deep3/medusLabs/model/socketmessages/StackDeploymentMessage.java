package com.deep3.medusLabs.model.socketmessages;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StackDeploymentMessage {

    private Long deployedLabId;
    private String stackName;
    private String accountName;
    private String accountId;
    private String message;

    public StackDeploymentMessage(Long deployedLabId, String stackName, String accountName, String accountId, String message) {
        this.stackName = stackName;
        this.accountName = accountName;
        this.accountId = accountId;
        this.message = message;
        this.deployedLabId = deployedLabId;
    }

    public String getStackName() {
        return stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDeployedLabId() {
        return deployedLabId;
    }

    public void setDeployedLabId(Long deployedLabId) {
        this.deployedLabId = deployedLabId;
    }
}
