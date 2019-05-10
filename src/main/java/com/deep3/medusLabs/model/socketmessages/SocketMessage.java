package com.deep3.medusLabs.model.socketmessages;

public class SocketMessage<T> {

    private T body;
    private String type;

    public SocketMessage(T body) {
        this.type = body.getClass().getSimpleName();
        this.body = body;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
