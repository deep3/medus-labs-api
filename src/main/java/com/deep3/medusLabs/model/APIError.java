package com.deep3.medusLabs.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

public class APIError {

    private HttpStatus status;
    private int statusCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;
    private String uri;

    private APIError() {
        this.timestamp = LocalDateTime.now();
    }

    public APIError(HttpStatus status) {
        this(status, null, null);
    }

    public APIError(HttpStatus status, Throwable ex) {
        this(status, ex, null);
    }

    public APIError(HttpStatus status, Throwable ex, WebRequest request) {
        this.timestamp = LocalDateTime.now();
        this.status = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        this.statusCode = this.status.value();
        this.message = ex != null ? ex.getMessage() : null;
        this.debugMessage = ex != null ? ex.getLocalizedMessage() : null;
        this.uri = request != null ? ((ServletWebRequest) request).getRequest().getRequestURI() : null;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
        this.statusCode = status.value();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.status = HttpStatus.valueOf(statusCode);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public void setDebugMessage(String debugMessage) {
        this.debugMessage = debugMessage;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
