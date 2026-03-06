package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.orchestration.PageTask;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.model.TMCPayloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static io.camunda.connector.TMCHttpClient.GET_TASKS_API;

public class GetAvailableTasksExecution extends AbstractConnectorExecution<PageTask, TMCPayloadRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetAvailableTasksExecution.class);

    @Override
    public PageTask execute() throws TMCConnectorException {
        LOGGER.info("Process: Get available tasks request");

        Map<String, Object> queryParams = Map.of();
        if (request.payload() != null && request.payload().queryParameters() != null) {
            queryParams = request.payload().queryParameters();
        }

        client.tmcAuthenticate(request.authentication());
        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                queryParams,
                GET_TASKS_API);

        var result = client.sendTMCGetRequest(uri, PageTask.class);

        LOGGER.info("Completed: get available tasks request");
        LOGGER.debug("Get available tasks request result: {}", result);

        return result;
    }

}
