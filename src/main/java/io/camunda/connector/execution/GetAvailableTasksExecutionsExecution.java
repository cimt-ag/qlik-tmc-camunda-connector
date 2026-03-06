package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.PageTaskExecutionStatus;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.model.TMCPayloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static io.camunda.connector.TMCHttpClient.AVAILABLE_TASKS_EXECUTIONS_API;

public class GetAvailableTasksExecutionsExecution extends AbstractConnectorExecution<PageTaskExecutionStatus, TMCPayloadRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetAvailableTasksExecutionsExecution.class);

    @Override
    public PageTaskExecutionStatus execute() throws TMCConnectorException {
        LOGGER.info("Process: Get available Tasks Executions");

        client.tmcAuthenticate(request.authentication());

        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                Map.of(),
                AVAILABLE_TASKS_EXECUTIONS_API);

        var result = client.sendTMCPostRequest(
                uri,
                request.payload().body(),
                PageTaskExecutionStatus.class);

        LOGGER.info("Completed: Get available Tasks Executions");
        LOGGER.debug("Get available Tasks Executions: {}", result);

        return result;
    }
}
