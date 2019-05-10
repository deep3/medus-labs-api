package com.deep3.medusLabs.aws.exceptions;

public class InvalidPasswordComplexity extends Exception{

    public InvalidPasswordComplexity()
    {
        super("Password complexity rules not met. The password must have a minimum length of 8," +
                " contain at least '1' upper case characters and at least '1' symbol.");
    }
}
