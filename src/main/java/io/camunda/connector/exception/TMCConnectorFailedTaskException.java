package io.camunda.connector.exception;

public class TMCConnectorFailedTaskException extends TMCConnectorException {
    public TMCConnectorFailedTaskException(String message) {
        super(message);
    }
    public TMCConnectorFailedTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
