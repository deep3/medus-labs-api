package com.deep3.medusLabs.model.socketmessages;

public class FinaliseDeploymentMessage {

    private Long deployedLabId;
    private String message;

    public FinaliseDeploymentMessage(Long deployedLabId, String message) {
        this.deployedLabId = deployedLabId;
        this.message = message;
    }

    public Long getDeployedLabId() {
        return deployedLabId;
    }

    public void setDeployedLabId(Long deployedLabId) {
        this.deployedLabId = deployedLabId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
