package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.exception.TMCConnectorException;

public abstract class AbstractConnectorExecution<T, P> {

    protected TMCHttpClient client;

    protected P request;

    public abstract T execute() throws TMCConnectorException;

    public AbstractConnectorExecution<T,P> client(TMCHttpClient client) {
        this.client = client;
        return this;
    }

    public AbstractConnectorExecution<T,P> request(P request) {
        this.request = request;
        return this;
    }
}
