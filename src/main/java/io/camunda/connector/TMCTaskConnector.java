package io.camunda.connector;

import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.orchestration.PageTask;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.connector.api.processing.Executionidentifier;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.model.ExecuteTaskRequest;
import io.camunda.connector.model.GetAvailableTaskRequest;
import io.camunda.connector.model.GetTaskExecutionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static io.camunda.connector.TMCHttpClient.*;

@OutboundConnector(name = "Qlik TMC Connector", type = "io.camunda:cimt-qlik-tmc-outbound-connector")
@ElementTemplate(
        name = "TMC outbound connector",
        id = "io.camunda.cimt.qlik-tmc-outbound-connector.v1",
        version = 1)
public class TMCTaskConnector implements OutboundConnectorProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TMCTaskConnector.class);

    final TMCHttpClient client =  new TMCHttpClient();

    @Operation(id = "getTasks", name = "get available tasks")
    public PageTask getAvailableTasksRequest(@Variable GetAvailableTaskRequest request) {
        LOGGER.info("Process: Get available tasks request");

        final String bearerToken = client.tmcAuthenticate(request.authentication());
        final URI uri = TMCHttpClient.createUri(request.endpoint(), request.payload().queryParameters(), GET_TASKS_API);

        var result = client.sendTMCGetRequest(uri, bearerToken, PageTask.class);

        LOGGER.info("Completed: get available tasks request");
        LOGGER.debug("Get available tasks request result: {}", result);

        return result;
    }

    @Operation(id = "executeTask", name = "Execute Task")
    public Executionidentifier executeTask(@Variable ExecuteTaskRequest request) {
        LOGGER.info("Process: execute task");

        final String bearerToken = client.tmcAuthenticate(request.authentication());
        final URI uri = TMCHttpClient.createUri(request.endpoint(), request.payload().queryParameters(), EXECUTE_TASK_API);

        // TODO: await Execution Status
        var result = client.sendTMCPostRequest(uri, request.payload().body(), bearerToken, Executionidentifier.class);

        LOGGER.info("Completed: execute task request");
        LOGGER.debug("Execute task request result: {}", result);

        return result;
    }

    @Operation(id = "getTaskExecutionStatus", name = "Get Task Execution Status")
    public JobExecutionStatusV21 getTaskExecutionStatus(@Variable GetTaskExecutionRequest request, @Variable(name = "executionId") String executionId) {
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
