package io.camunda.connector.exception;

public class TMCErrorResponseException extends TMCConnectorException {
    public TMCErrorResponseException(String message) {
        super(message);
    }
    public TMCErrorResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
