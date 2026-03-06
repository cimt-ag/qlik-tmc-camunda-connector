package io.camunda.connector;

import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.error.ConnectorExceptionBuilder;
import io.camunda.connector.api.orchestration.PageTask;
import io.camunda.connector.api.orchestration.TaskV21;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.api.processing.PageTaskExecutionStatus;
import io.camunda.connector.exception.*;
import io.camunda.connector.execution.*;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.model.TMCBasicRequest;
import io.camunda.connector.model.TMCPayloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOGGER.info("Get available tasks request");
        return execute(new GetAvailableTasksExecution()
                .client(client)
                .request(request));
    }

    @Operation(id = "executeTask", name = "Execute Task")
    public JobExecutionStatusV21 executeTask(@Variable TMCPayloadRequest request,
                                             @Variable(name = "offset", value = "60") Integer offset,
                                             @Variable(name = "period", value = "60") Integer period,
                                             @Variable(name = "limit", value = "100") Integer limit
    ) {
        LOGGER.info("Execute Task request");
        return execute(new ExecuteTaskExecution()
                .args(period, offset, limit)
                .client(client)
                .request(request)
        );
    }

    @Operation(id = "getTaskExecutionStatus", name = "Get Task Execution Status")
    public JobExecutionStatusV21 getTaskExecutionStatus(@Variable TMCBasicRequest request,
                                                        @Variable(name = "getTaskExecutionStatus_executionId") String executionId
    ) {
        LOGGER.info("Get Task Execution Status request");
        return execute(new GetTaskExecutionStatusExecution()
                .args(executionId)
                .client(client)
                .request(request)
        );
    }

    @Operation(id = "getAvailableTasksExecutions", name = "Get available Tasks Executions")
    public PageTaskExecutionStatus getAvailableTasksExecutions(@Variable TMCPayloadRequest request) {
        LOGGER.info("Get Available Tasks Executions request");
        return execute(new GetAvailableTasksExecutionsExecution()
                .client(client)
                .request(request)
        );
    }

    @Operation(id = "getTaskExecutions", name = "Get Task Executions")
    public PageTaskExecutionStatus getTaskExecutions(@Variable TMCPayloadRequest request,
                                                     @Variable(name = "getTaskExecutions_taskId") String taskId) {
        LOGGER.info("Get Task Executions request");
        return execute(new GetTaskExecutionsExecution()
                .args(taskId)
                .client(client)
                .request(request));
    }

    @Operation(id = "getTaskById", name = "Get Task by ID")
    public TaskV21 getTaskById(@Variable TMCBasicRequest request,
                               @Variable(name = "getTaskById_taskId") String taskId) {
        LOGGER.info("Get Task by id request");
        return execute(new GetTaskByIdExecution()
                .args(taskId)
                .client(client)
                .request(request));
    }

    @Operation(id = "terminateTaskExecution", name = "Terminate Task Execution")
    public void terminateTaskExecution(@Variable TMCBasicRequest request,
                                       @Variable(name = "terminateTaskExecution_executionId") String executionId) {
        LOGGER.info("Terminate Task Execution request");
        execute(new TerminateTaskExecution()
                .args(executionId)
                .client(client)
                .request(request));
    }

    private <T> T execute(AbstractConnectorExecution<T, ?> execution) throws ConnectorException {
        final String taskExecutionFailed = "TASK_EXECUTION_FAILED";
        final String tmcConnectionFailed = "TMC_CONNECTION_FAILED";
        final String tmcErrorResponse = "TMC_REPONSE_ERROR";
        final String tmcConnectorProcessingError = "TMC_CONNECTOR_PROCESSING_ERROR";
        final String tmcTechnicalError = "TMC_TECHNICAL_ERROR";

        try {
            return execution.execute();
        } catch (TMCConnectorFailedTaskException e) {
            throw buildException(e, taskExecutionFailed);
        } catch (TMCConnectionException e) {
            throw buildException(e, tmcConnectionFailed);
        } catch (TMCErrorResponseException e) {
            throw buildException(e, tmcErrorResponse);
        } catch (TMCConnectorProcessingException e) {
            throw buildException(e, tmcConnectorProcessingError);
        } catch (TMCConnectorException | RuntimeException e) {
            throw buildException(e, tmcTechnicalError);
        }
    }

    private ConnectorException buildException(Throwable e, String errorCode) {
        return new ConnectorExceptionBuilder()
                .message(e.getMessage())
                .errorCode(errorCode)
                .cause(e)
                .build();
    }

}
