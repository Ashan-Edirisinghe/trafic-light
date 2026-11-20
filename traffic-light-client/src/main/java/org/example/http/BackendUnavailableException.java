package org.example.http;

public class BackendUnavailableException extends Exception {
    public BackendUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}