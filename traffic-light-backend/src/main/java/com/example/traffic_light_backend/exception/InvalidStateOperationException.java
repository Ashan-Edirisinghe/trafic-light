package com.example.traffic_light_backend.exception;

public class InvalidStateOperationException extends RuntimeException {

    public InvalidStateOperationException(String message) {
        super(message);
    }
}
