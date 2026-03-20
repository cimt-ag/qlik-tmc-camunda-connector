package io.camunda.connector.execution;

import io.camunda.connector.exception.TMCConnectorException;

public interface TMCConnectorExecution<T> {

    T execute() throws TMCConnectorException;
}
