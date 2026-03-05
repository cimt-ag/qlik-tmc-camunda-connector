package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.exception.TMCConnectionException;
import io.camunda.connector.exception.TMCConnectorProcessingException;
import io.camunda.connector.model.TMCBasicRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static io.camunda.connector.TMCHttpClient.EXECUTION_STATUS_API;

public class GetTaskExecutionStatusExecution extends AbstractConnectorExecution<JobExecutionStatusV21, TMCBasicRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetTaskExecutionStatusExecution.class);

    private String executionId;

    public GetTaskExecutionStatusExecution args(String executionId) {
        this.executionId = executionId;
        return this;
    }

    @Override
    public JobExecutionStatusV21 execute() throws TMCConnectionException, TMCConnectorProcessingException {
        LOGGER.info("Process: Get task execution status");

        final String bearerToken = client.tmcAuthenticate(request.authentication());

        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                Map.of(),
                EXECUTION_STATUS_API.apply(executionId));

        var result = client.sendTMCGetRequest(uri, bearerToken, JobExecutionStatusV21.class);

        LOGGER.info("Completed: get task execution status request");
        LOGGER.debug("Task execution status result: {}", result);

        return result;
    }
}
