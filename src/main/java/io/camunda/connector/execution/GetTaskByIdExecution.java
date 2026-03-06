package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.orchestration.TaskV21;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.model.TMCBasicRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static io.camunda.connector.TMCHttpClient.GET_TASK_BY_ID_API;

public class GetTaskByIdExecution extends AbstractConnectorExecution<TaskV21, TMCBasicRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetTaskByIdExecution.class);

    private String taskId;

    public GetTaskByIdExecution args(String taskId) {
        this.taskId = taskId;
        return this;
    }

    @Override
    public TaskV21 execute() throws TMCConnectorException {
        LOGGER.info("Process: Get task by Id");
        client.tmcAuthenticate(request.authentication());

        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                Map.of(),
                GET_TASK_BY_ID_API.apply(taskId));

        var result = client.sendTMCGetRequest(uri, TaskV21.class);

        LOGGER.info("Completed: get task by id request");
        LOGGER.debug("Task by id result: {}", result);

        return result;
    }
}
