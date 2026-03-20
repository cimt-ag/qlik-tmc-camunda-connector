package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;

public abstract class AbstractConnectorExecution<T, P> implements TMCConnectorExecution<T> {

    protected TMCHttpClient client;
    protected P request;

    public static abstract class AbstractExecutionBuilder<T,P> {
        protected TMCHttpClient client;
        protected P request;

        public AbstractExecutionBuilder<T,P> client(TMCHttpClient client) {
            this.client = client;
            return this;
        }

        public AbstractExecutionBuilder<T,P> request(P request) {
            this.request = request;
            return this;
        }

        protected void setParameter(AbstractConnectorExecution<T,P> execution) {
            if (request == null) {
                throw new IllegalArgumentException("Missing request parameter");
            }

            execution.request = request;
            execution.client = this.client == null ? new TMCHttpClient() : this.client;
        }

        public abstract TMCConnectorExecution<T> build();

    }
}
