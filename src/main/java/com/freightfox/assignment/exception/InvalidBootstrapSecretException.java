package com.freightfox.assignment.exception;

public class InvalidBootstrapSecretException extends RuntimeException {

    public InvalidBootstrapSecretException() {
        super("Invalid admin registration secret");
    }
}