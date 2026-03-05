package io.camunda.connector.exception;

public class TMCConnectorProcessingException extends TMCConnectorException {
    public TMCConnectorProcessingException(String message) {
        super(message);
    }
    public TMCConnectorProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
