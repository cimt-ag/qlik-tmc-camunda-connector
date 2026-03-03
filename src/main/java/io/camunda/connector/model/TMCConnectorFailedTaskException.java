package io.camunda.connector.model;

public class TMCConnectorFailedTaskException extends Exception {
    public TMCConnectorFailedTaskException(String message) {
        super(message);
    }
    public TMCConnectorFailedTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
