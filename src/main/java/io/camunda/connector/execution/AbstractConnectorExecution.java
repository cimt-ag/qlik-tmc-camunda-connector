package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;

public abstract class AbstractConnectorExecution<T, P> implements TMCConnectorExecution<T, P> {

    protected TMCHttpClient client;

    protected P request;

    @Override
    public AbstractConnectorExecution<T, P> client(TMCHttpClient client) {
        this.client = client;
        return this;
    }

    @Override
    public AbstractConnectorExecution<T, P> request(P request) {
        this.request = request;
        return this;
    }
}
