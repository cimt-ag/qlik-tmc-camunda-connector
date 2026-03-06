package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.PageTaskExecutionStatus;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.model.TMCPayloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static io.camunda.connector.TMCHttpClient.TASK_EXECUTIONS_API;

public class GetTaskExecutionsExecution extends AbstractConnectorExecution<PageTaskExecutionStatus, TMCPayloadRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetTaskExecutionsExecution.class);

    private String token;
    private String taskId;

    public GetTaskExecutionsExecution args(String taskId, String token) {
        this.token = token;
        this.taskId = taskId;
        return this;
    }

    @Override
    public PageTaskExecutionStatus execute() throws TMCConnectorException {
        LOGGER.info("Process: Get task execution");

        final String bearerToken = token != null && !token.isEmpty() ?
                token : client.tmcAuthenticate(request.authentication());

        Map<String, Object> queryParams = Map.of();
        if (request.payload() != null && request.payload().queryParameters() != null) {
            queryParams = request.payload().queryParameters();
        }

        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                queryParams,
                TASK_EXECUTIONS_API.apply(taskId));

        var result = client.sendTMCGetRequest(uri, bearerToken, PageTaskExecutionStatus.class);

        LOGGER.info("Completed: get task execution request");
        LOGGER.debug("Task execution result: {}", result);

        return result;
    }
}
