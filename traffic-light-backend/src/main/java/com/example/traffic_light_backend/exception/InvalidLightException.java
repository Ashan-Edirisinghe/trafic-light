package com.example.traffic_light_backend.exception;

public class InvalidLightException extends RuntimeException {

    public InvalidLightException(String message) {
        super(message);
    }
}
