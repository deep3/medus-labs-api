package com.deep3.medusLabs.aws.exceptions;

public class NoAvailableServiceComponent extends Exception {

    public NoAvailableServiceComponent() {
        super("A service component with active credentials cannot be found. Ensure the service component is available for use.");
    }
}
