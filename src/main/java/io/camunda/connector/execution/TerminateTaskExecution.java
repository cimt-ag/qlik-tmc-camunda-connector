package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.model.TMCBasicRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static io.camunda.connector.TMCHttpClient.TERMINATE_TASK_EXECUTION_API;

public class TerminateTaskExecution extends AbstractConnectorExecution<Void, TMCBasicRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateTaskExecution.class);

    private String executionId;

    public TerminateTaskExecution args(String executionId) {
        this.executionId = executionId;
        return this;
    }

    @Override
    public Void execute() throws TMCConnectorException {
        LOGGER.info("Process: Terminate Execution {}", executionId);
        client.tmcAuthenticate(request.authentication());

        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                Map.of(),
                TERMINATE_TASK_EXECUTION_API.apply(executionId));

        client.sendTMCDeleteRequest(uri);

        LOGGER.info("Completed: Terminate Tasks Execution");

        return null;
    }
}
