package com.deep3.medusLabs.aws.exceptions;

/**
 * Generic exception thrown when the client has made a Bad HTTP request
 */
public class BadRequestException extends Exception {

    public BadRequestException() {
        this("Received bad HTTP request from the client");
    }

    public BadRequestException(String message) {
        super(message);
    }
}
