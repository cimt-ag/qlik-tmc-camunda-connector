package io.camunda.connector.model;

public class TMCConnectionException extends RuntimeException {

    public TMCConnectionException() {
    }

    public TMCConnectionException(String message) {
        super(message);
    }

    public TMCConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
