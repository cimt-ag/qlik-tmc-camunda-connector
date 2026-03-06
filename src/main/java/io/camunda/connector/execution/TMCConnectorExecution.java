package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.exception.TMCConnectorException;

public interface TMCConnectorExecution<T, P> {

    T execute() throws TMCConnectorException;

    TMCConnectorExecution<T, P> client(TMCHttpClient client);

    TMCConnectorExecution<T, P> request(P request);
}
