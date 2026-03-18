package io.camunda.connector.exception;

public class TMCTaskExecutionDetachException extends TMCConnectorException {
    public TMCTaskExecutionDetachException(String message) {
        super(message);
    }
    public TMCTaskExecutionDetachException(String message, Throwable cause) {
        super(message, cause);
    }
}
