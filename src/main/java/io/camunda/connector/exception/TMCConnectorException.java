package io.camunda.connector.exception;

public class TMCConnectorException extends Exception {

    public  TMCConnectorException() {}

    public TMCConnectorException(String message) {
        super(message);
    }

    public TMCConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
