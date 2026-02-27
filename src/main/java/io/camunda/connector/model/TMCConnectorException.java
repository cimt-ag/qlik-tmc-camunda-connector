package io.camunda.connector.model;

public class TMCConnectorException extends RuntimeException {

    public TMCConnectorException(String message) {
        super(message);
    }

    public TMCConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
