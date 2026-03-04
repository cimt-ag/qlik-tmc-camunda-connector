package io.camunda.connector;

import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.orchestration.PageTask;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.connector.api.processing.Executionidentifier;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.api.processing.PageTaskExecutionStatus;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.model.TMCBasicRequest;
import io.camunda.connector.model.TMCConnectorException;
import io.camunda.connector.model.TMCConnectorFailedTaskException;
import io.camunda.connector.model.TMCPayloadRequest;
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

    private final TMCHttpClient client = new TMCHttpClient();

    @Operation(id = "getTasks", name = "get available tasks")
    public PageTask getAvailableTasks(@Variable TMCPayloadRequest request) {
        LOGGER.info("Process: Get available tasks request");

        final String bearerToken = client.tmcAuthenticate(request.authentication());
        final URI uri = TMCHttpClient.createUri(request.endpoint(), request.payload().queryParameters(), GET_TASKS_API);

        var result = client.sendTMCGetRequest(uri, bearerToken, PageTask.class);

        LOGGER.info("Completed: get available tasks request");
        LOGGER.debug("Get available tasks request result: {}", result);

        return result;
    }

    @Operation(id = "executeTask", name = "Execute Task")
    public JobExecutionStatusV21 executeTask(@Variable TMCPayloadRequest request,
                                             @Variable(name = "offset", value = "60") Integer offset,
                                             @Variable(name = "period", value = "60") Integer period,
                                             @Variable(name = "limit", value = "100") Integer limit) throws Exception {
        LOGGER.info("Process: execute task");

        final String bearerToken = client.tmcAuthenticate(request.authentication());
        final URI uri = TMCHttpClient.createUri(request.endpoint(), request.payload().queryParameters(), EXECUTE_TASK_API);

        var executionidentifier = client.sendTMCPostRequest(uri, request.payload().body(), bearerToken, Executionidentifier.class);

        final int offsetInMillis = offset * 1000;

        var result = checkExecutionStatus(
                new TMCBasicRequest(
                        request.authentication(),
                        request.endpoint()
                ),
                executionidentifier.getExecutionId(),
                offsetInMillis,
                period,
                limit
        );

        LOGGER.info("Completed: execute task request");
        LOGGER.debug("Execute task request result: {}", result);

        return result;
    }

    private JobExecutionStatusV21 checkExecutionStatus(TMCBasicRequest request,
                                                       String executionId,
                                                       Integer offsetInMillis,
                                                       Integer period,
                                                       Integer limit) throws Exception {
        LOGGER.info("Process: check execution status");
        try {
            LOGGER.debug("Wait for {} seconds to check for task execution status", offsetInMillis);
            Thread.sleep(offsetInMillis);
        } catch (InterruptedException e) {
            throw new TMCConnectorException(String.format("Error while awaiting offset: %s", e.getMessage()), e);
        }

        JobExecutionStatusV21 result;
        int i = 0;

        do {
            i++;
            LOGGER.debug("{} try checking status of the task execution", i);
            result = getTaskExecutionStatus(request, executionId);
            if (result != null && taskExecutionIsDone(result.getExecutionStatus())) {
                break;
            } else {
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    throw new TMCConnectorException(
                            String.format("Error while awaiting finishing Task: %s", e.getMessage()), e);
                }
            }
        } while (i <= limit);

        if (i > limit) {
            LOGGER.info("Retry Limits reached - detaching connector");
            throw new TMCConnectorFailedTaskException("Retry Limits reached - detaching connector");
        } else if (taskExecutionFailed(result.getExecutionStatus())) {
            LOGGER.info("Failed Task with Status {}", result.getExecutionStatus().getValue());
            throw new TMCConnectorFailedTaskException(
                    String.format("Failed Task Execution - %s", result.getErrorMessage()));
        } else {
            LOGGER.info("Finished Task successful with Status {}", result.getExecutionStatus().getValue());
        }

        return result;
    }

    private boolean taskExecutionIsDone(JobExecutionStatusV21.ExecutionStatusEnum executionStatus) {
        return switch (executionStatus) {
            case EXECUTION_EVENT_RECEIVED, DISPATCHING_FLOW, STARTING_FLOW_EXECUTION, STOPPING_FLOW_EXECUTION -> false;
            default -> true;
        };
    }

    private boolean taskExecutionFailed(JobExecutionStatusV21.ExecutionStatusEnum executionStatus) {
        return JobExecutionStatusV21.ExecutionStatusEnum.EXECUTION_SUCCESS != executionStatus;
    }

    @Operation(id = "getTaskExecutionStatus", name = "Get Task Execution Status")
    public JobExecutionStatusV21 getTaskExecutionStatus(@Variable TMCBasicRequest request,
                                                        @Variable(name = "getTaskExecutionStatus_executionId") String executionId) {
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

    @Operation(id = "getAvailableTasksExecutions", name = "Get available Tasks Executions")
    public PageTaskExecutionStatus getAvailableTasksExecutions(@Variable TMCPayloadRequest request) {
        LOGGER.info("Process: Get available Tasks Executions");

        final String bearerToken = client.tmcAuthenticate(request.authentication());

        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                Map.of(),
                AVAILABLE_TASKS_EXECUTIONS_API);

        var result = client.sendTMCPostRequest(
                uri,
                request.payload().body(),
                bearerToken,
                PageTaskExecutionStatus.class);

        LOGGER.info("Completed: Get available Tasks Executions");
        LOGGER.debug("Get available Tasks Executions: {}", result);

        return result;
    }

    @Operation(id = "getTaskExecutions", name = "Get Task Executions")
    public PageTaskExecutionStatus getTaskExecutions(@Variable TMCPayloadRequest request,
                                                     @Variable(name = "getTaskExecutions_taskId") String taskId) {
        LOGGER.info("Process: Get task execution");

        final String bearerToken = client.tmcAuthenticate(request.authentication());


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

    @Operation(id = "terminateTaskExecution", name = "Execution ID to terminate Execution")
    public void terminateTaskExecution(@Variable TMCBasicRequest request,
                                       @Variable(name = "terminateTaskExecution_executionId") String executionId) {
        LOGGER.info("Process: Terminate Execution {}", executionId);
        final String bearerToken = client.tmcAuthenticate(request.authentication());

        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                Map.of(),
                AVAILABLE_TASKS_EXECUTIONS_API);

        client.sendTMCDeleteRequest(
                uri,
                bearerToken);

        LOGGER.info("Completed: Terminate Tasks Execution");
    }

}
