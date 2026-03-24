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

    private String taskId;

    @Override
    public PageTaskExecutionStatus execute() throws TMCConnectorException {
        LOGGER.info("Process: Get task execution");

        processAuthenticate(request);

        Map<String, Object> queryParams = Map.of();
        if (request.payload() != null && request.payload().queryParameters() != null) {
            queryParams = request.payload().queryParameters();
        }

        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                queryParams,
                TASK_EXECUTIONS_API.apply(taskId));

        var result = client.sendTMCGetRequest(uri, PageTaskExecutionStatus.class);

        LOGGER.info("Completed: get task execution request");
        LOGGER.debug("Task execution result: {}", result);

        return result;
    }

    public static class Builder extends AbstractExecutionBuilder<PageTaskExecutionStatus, TMCPayloadRequest> {

        private String taskId;

        public Builder args(String taskId) {
            this.taskId = taskId;
            return this;
        }

        @Override
        public TMCConnectorExecution<PageTaskExecutionStatus> build() {
            var execution = new GetTaskExecutionsExecution();

            setParameter(execution);

            if (taskId == null || taskId.isEmpty()) {
                throw new IllegalArgumentException("TaskId is required");
            }

            execution.taskId = taskId;

            return execution;
        }
    }
}
