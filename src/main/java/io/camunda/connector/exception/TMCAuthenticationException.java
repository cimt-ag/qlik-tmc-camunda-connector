package io.camunda.connector.exception;

public class TMCAuthenticationException extends TMCConnectorException {
    public TMCAuthenticationException(String message) {
        super(message);
    }

    public TMCAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TMCAuthenticationException() {
        super();
    }
}
