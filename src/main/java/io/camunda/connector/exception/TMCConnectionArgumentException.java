package io.camunda.connector.exception;

public class TMCConnectionArgumentException extends TMCConnectorException {
    public TMCConnectionArgumentException(String message) {
        super(message);
    }
    public TMCConnectionArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
