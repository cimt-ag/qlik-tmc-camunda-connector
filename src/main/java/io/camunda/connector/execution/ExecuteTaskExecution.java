package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.Executionidentifier;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.api.processing.TaskExecutionStatus;
import io.camunda.connector.exception.TMCConnectionArgumentException;
import io.camunda.connector.exception.TMCConnectionException;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.exception.TMCTaskExecutionDetachException;
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

    @Override
    public JobExecutionStatusV21 execute() throws TMCConnectorException {
        LOGGER.info("Process: execute task");

        processAuthenticate(request);
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

        var lastExecutions = new GetTaskExecutionsExecution.Builder()
                .args(taskId)
                .client(client)
                .request(new TMCPayloadRequest(
                        authentication,
                        endpoint,
                        TMCPayload.emptyPayload()))
                .build()
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
     * @throws TMCConnectionException          if the connection to the TMC fails
     * @throws TMCTaskExecutionDetachException if the retry limit exceeds
     * @throws TMCConnectionArgumentException if the await for the offset or the period fails
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
            throw new TMCConnectionArgumentException(String.format("Error while awaiting offset: %s", e.getMessage()), e);
        }

        JobExecutionStatusV21 result = null;
        int i = 0;

        while (i <= limit) {
            LOGGER.debug("{} try checking status of the task execution", i);

            result = getExecutionStatus(executionId);

            if (result != null && isTaskExecutionDone(result.getExecutionStatus())) {
                break;
            } else {
                try {
                    Thread.sleep(periodInMillis);
                } catch (InterruptedException e) {
                    throw new TMCConnectionArgumentException(
                            String.format(
                                    "Error while awaiting of Task execution - %s: %s",
                                    executionId,
                                    e.getMessage()),
                            e);
                }
            }
            i++;
        }

        if (limit < i) {
            LOGGER.info("Retry Limits reached - detaching connector");
            throw new TMCTaskExecutionDetachException(
                    String.format("TMC Retry Limits reached - detaching connector from execution %s", executionId));
        }

        LOGGER.info("Finished Task Execution {} successful with Status {}", executionId, result.getExecutionStatus().getValue());

        return result;
    }

    private JobExecutionStatusV21 getExecutionStatus(String executionId) throws TMCConnectorException {
        return new GetTaskExecutionStatusExecution.Builder()
                .args(executionId)
                .client(client)
                .request(request.createBasicRequest())
                .build()
                .execute();
    }

    private boolean isTaskExecutionDone(JobExecutionStatusV21.ExecutionStatusEnum executionStatus) {
        return switch (executionStatus) {
            case EXECUTION_EVENT_RECEIVED, DISPATCHING_FLOW, STARTING_FLOW_EXECUTION, STOPPING_FLOW_EXECUTION -> false;
            default -> true;
        };
    }

    public static class Builder extends AbstractExecutionBuilder<JobExecutionStatusV21, TMCPayloadRequest> {

        private Integer period = 60;
        private Integer offset = 60;
        private Integer limit = 100;

        public Builder args(Integer period, Integer offset, Integer limit) {
            this.period = period;
            this.offset = offset;
            this.limit = limit;
            return this;
        }

        @Override
        public TMCConnectorExecution<JobExecutionStatusV21> build() {
            var execution = new ExecuteTaskExecution();

            setParameter(execution);

            execution.period = this.period;
            execution.offset = this.offset;
            execution.limit = this.limit;

            return execution;
        }
    }

}
