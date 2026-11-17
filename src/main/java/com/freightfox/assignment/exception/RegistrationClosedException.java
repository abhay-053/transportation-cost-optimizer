package com.freightfox.assignment.exception;

public class RegistrationClosedException extends RuntimeException {

    public RegistrationClosedException() {
        super("Admin registration is already closed");
    }
}