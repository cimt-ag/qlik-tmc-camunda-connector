package io.camunda.connector.exception;

public class TMCConnectionException extends TMCConnectorException {

    public TMCConnectionException() {
        super();
    }

    public TMCConnectionException(String message) {
        super(message);
    }

    public TMCConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
