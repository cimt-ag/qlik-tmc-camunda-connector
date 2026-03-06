package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.Executionidentifier;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.api.processing.TaskExecutionStatus;
import io.camunda.connector.exception.TMCConnectionException;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.exception.TMCConnectorFailedTaskException;
import io.camunda.connector.exception.TMCConnectorProcessingException;
import io.camunda.connector.model.TMCAuthentication;
import io.camunda.connector.model.TMCEndpoint;
import io.camunda.connector.model.TMCPayload;
import io.camunda.connector.model.TMCPayloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;

import static io.camunda.connector.TMCHttpClient.EXECUTE_TASK_API;

public class ExecuteTaskExecution extends AbstractConnectorExecution<JobExecutionStatusV21, TMCPayloadRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteTaskExecution.class);

    public static final String EXECUTABLE_KEY = "executable";

    private Integer period;
    private Integer offset;
    private Integer limit;

    public ExecuteTaskExecution args(Integer period, Integer offset, Integer limit) {
        this.period = period;
        this.offset = offset;
        this.limit = limit;
        return this;
    }

    @Override
    public JobExecutionStatusV21 execute() throws TMCConnectorException {
        LOGGER.info("Process: execute task");

        client.tmcAuthenticate(request.authentication());
        final URI uri = TMCHttpClient.createUri(
                request.endpoint(),
                request.payload().queryParameters(),
                EXECUTE_TASK_API);

        var currentTaskExecution = getCurrentExecution(
                request.authentication(),
                request.endpoint(),
                String.valueOf(request.payload().body().get(EXECUTABLE_KEY)));

        String executionId;

        // if there is a current execution it is likely this execution is a connector retry
        // therefor we want to monitor this execution
        if (currentTaskExecution.isPresent()) {
            executionId = currentTaskExecution.get().getExecutionId();
        } else {
            var executionidentifier = client.sendTMCPostRequest(
                    uri,
                    request.payload().body(),
                    Executionidentifier.class);
            if (executionidentifier == null) {
                throw new TMCConnectionException("Empty Execution Identifier after task execution");
            }
            executionId = executionidentifier.getExecutionId();
        }

        final int offsetInMillis = offset * 1000;
        final int periodInMillis = period * 1000;

        var result = checkExecutionStatus(
                executionId,
                offsetInMillis,
                periodInMillis,
                limit
        );

        LOGGER.info("Completed: execute task request");
        LOGGER.debug("Execute task request result: {}", result);

        return result;
    }

    private Optional<TaskExecutionStatus> getCurrentExecution(
            final TMCAuthentication authentication,
            final TMCEndpoint endpoint,
            final String taskId) throws TMCConnectorException {
        if (taskId == null || taskId.isEmpty()) {
            LOGGER.debug("No task id provided");
            throw new IllegalArgumentException("Missing taskId - Please provide a task ID to execute the task");
        }

        var lastExecutions = new GetTaskExecutionsExecution()
                .args(taskId)
                .client(client)
                .request(new TMCPayloadRequest(
                        authentication,
                        endpoint,
                        TMCPayload.emptyPayload()))
                .execute();
        return lastExecutions.getItems().stream()
                .filter(item -> item.getStatus() == TaskExecutionStatus.StatusEnum.EXECUTING)
                .findFirst();
    }

    /**
     * Checks if the current execution is executed successful with an offset and a period within the connector
     * tries to check the execution status
     *
     * @return the status of the finished Execution
     * @throws TMCConnectorFailedTaskException if the connection to the TMC fails
     * @throws TMCConnectorProcessingException if the await for the offset or the period fails
     */
    private JobExecutionStatusV21 checkExecutionStatus(String executionId,
                                                       Integer offsetInMillis,
                                                       Integer periodInMillis,
                                                       Integer limit
    ) throws TMCConnectorException {
        LOGGER.info("Process: check execution status");
        try {
            LOGGER.debug("Wait for {} seconds to check for task execution status", offsetInMillis);
            Thread.sleep(offsetInMillis);
        } catch (InterruptedException e) {
            throw new TMCConnectorProcessingException(String.format("Error while awaiting offset: %s", e.getMessage()), e);
        }

        JobExecutionStatusV21 result;
        int i = 0;

        do {
            i++;
            LOGGER.debug("{} try checking status of the task execution", i);

            result = new GetTaskExecutionStatusExecution()
                    .args(executionId)
                    .client(client)
                    .request(request.createBasicRequest())
                    .execute();

            if (result != null && isTaskExecutionDone(result.getExecutionStatus())) {
                break;
            } else {
                try {
                    Thread.sleep(periodInMillis);
                } catch (InterruptedException e) {
                    throw new TMCConnectorProcessingException(
                            String.format("Error while awaiting finishing Task: %s", e.getMessage()), e);
                }
            }
        } while (i <= limit);

        if (limit < i) {
            LOGGER.info("Retry Limits reached - detaching connector");
            throw new TMCConnectorFailedTaskException("TMC Retry Limits reached - detaching connector");
        } else if (taskExecutionFailed(result.getExecutionStatus())) {
            LOGGER.info("Failed Task with Status {}", result.getExecutionStatus().getValue());
            throw new TMCConnectorFailedTaskException(String.format("Failed Task with Status %s", result.getErrorMessage()));
        } else {
            LOGGER.info("Finished Task successful with Status {}", result.getExecutionStatus().getValue());
        }

        return result;
    }

    private boolean canExecutionContinue(JobExecutionStatusV21.ExecutionStatusEnum executionStatus) {
        return switch (executionStatus) {
            case EXECUTION_REJECTED, DEPLOY_FAILED -> false;
            default -> true;
        };
    }

    private boolean isTaskExecutionDone(JobExecutionStatusV21.ExecutionStatusEnum executionStatus) {
        return switch (executionStatus) {
            case EXECUTION_EVENT_RECEIVED, DISPATCHING_FLOW, STARTING_FLOW_EXECUTION, STOPPING_FLOW_EXECUTION -> false;
            default -> true;
        };
    }

    private boolean taskExecutionFailed(JobExecutionStatusV21.ExecutionStatusEnum executionStatus) {
        return JobExecutionStatusV21.ExecutionStatusEnum.EXECUTION_SUCCESS != executionStatus;
    }
}
