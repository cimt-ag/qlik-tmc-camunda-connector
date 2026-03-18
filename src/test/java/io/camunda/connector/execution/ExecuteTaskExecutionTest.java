package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.Executionidentifier;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.api.processing.PageTaskExecutionStatus;
import io.camunda.connector.api.processing.TaskExecutionStatus;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.exception.TMCTaskExecutionDetachException;
import io.camunda.connector.model.TMCPayload;
import io.camunda.connector.model.TMCPayloadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static io.camunda.connector.TMCHttpClient.EXECUTION_STATUS_API;
import static io.camunda.connector.TMCHttpClient.TASK_EXECUTIONS_API;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecuteTaskExecutionTest extends AbstractTMCConnectorExecutionTest {

    @BeforeEach
    public void setup() {
        setupRequestModels(AUTH_TOKEN_MOCK);
    }

    @Test
    public void testExecuteTaskExecution() throws TMCConnectorException {
        final String executableId = "abc";
        final URI uri = TMCHttpClient.createUri(basicRequest.endpoint(), Map.of(), TMCHttpClient.EXECUTE_TASK_API);

        final Integer period = 1;
        final Integer offset = 0;
        final Integer limit = 5;

        Map<String, Object> body = Map.of(ExecuteTaskExecution.EXECUTABLE_KEY, executableId);

        // execute Task stubbing
        final String executionId = "executionId";
        Executionidentifier executionidentifier = mock(Executionidentifier.class);
        when(executionidentifier.getExecutionId()).thenReturn(executionId);
        when(mockClient.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
        when(mockClient.sendTMCPostRequest(uri, body, Executionidentifier.class)).thenReturn(executionidentifier);

        // getCurrentExecution stubbing
        final URI currentExecutionURI = TMCHttpClient.createUri(
                basicRequest.endpoint(), Map.of(), TASK_EXECUTIONS_API.apply(executableId));
        PageTaskExecutionStatus currentExecutionStatus = mock(PageTaskExecutionStatus.class);
        TaskExecutionStatus itemStatus = mock(TaskExecutionStatus.class);
        when(itemStatus.getStatus()).thenReturn(TaskExecutionStatus.StatusEnum.EXECUTION_SUCCESSFUL);
        when(currentExecutionStatus.getItems()).thenReturn(List.of(itemStatus));
        when(mockClient.sendTMCGetRequest(currentExecutionURI, PageTaskExecutionStatus.class))
                .thenReturn(currentExecutionStatus);

        // GetTaskExecutionStatusExecution stubbing
        final URI uriGetTaskExecutionStatusExecution = TMCHttpClient.createUri(
                basicRequest.endpoint(),
                Map.of(),
                EXECUTION_STATUS_API.apply(executionId));
        final JobExecutionStatusV21 resultStatus = mock(JobExecutionStatusV21.class);
        when(resultStatus.getExecutionStatus()).thenReturn(JobExecutionStatusV21.ExecutionStatusEnum.EXECUTION_SUCCESS);
        when(mockClient.sendTMCGetRequest(uriGetTaskExecutionStatusExecution, JobExecutionStatusV21.class))
                .thenReturn(resultStatus);

        var result = new ExecuteTaskExecution()
                .args(period, offset, limit)
                .client(mockClient)
                .request(new TMCPayloadRequest(
                        basicRequest.authentication(),
                        basicRequest.endpoint(),
                        new TMCPayload(Map.of(), body)
                ))
                .execute();

        assertEquals(resultStatus, result);
    }

    @Test
    public void testExecuteTaskThrowDetachException() throws TMCConnectorException {
        final String executableId = "abc";
        final URI uri = TMCHttpClient.createUri(basicRequest.endpoint(), Map.of(), TMCHttpClient.EXECUTE_TASK_API);

        final Integer period = 1;
        final Integer offset = 0;
        final Integer limit = 1;

        Map<String, Object> body = Map.of(ExecuteTaskExecution.EXECUTABLE_KEY, executableId);

        // execute Task stubbing
        final String executionId = "executionId";
        Executionidentifier executionidentifier = mock(Executionidentifier.class);
        when(executionidentifier.getExecutionId()).thenReturn(executionId);
        when(mockClient.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
        when(mockClient.sendTMCPostRequest(uri, body, Executionidentifier.class)).thenReturn(executionidentifier);

        // getCurrentExecution stubbing
        final URI currentExecutionURI = TMCHttpClient.createUri(
                basicRequest.endpoint(), Map.of(), TASK_EXECUTIONS_API.apply(executableId));
        PageTaskExecutionStatus currentExecutionStatus = mock(PageTaskExecutionStatus.class);
        TaskExecutionStatus itemStatus = mock(TaskExecutionStatus.class);
        when(itemStatus.getStatus()).thenReturn(TaskExecutionStatus.StatusEnum.EXECUTING);
        when(currentExecutionStatus.getItems()).thenReturn(List.of(itemStatus));
        when(mockClient.sendTMCGetRequest(currentExecutionURI, PageTaskExecutionStatus.class))
                .thenReturn(currentExecutionStatus, currentExecutionStatus);

        // GetTaskExecutionStatusExecution stubbing
        final URI uriGetTaskExecutionStatusExecution = TMCHttpClient.createUri(
                basicRequest.endpoint(),
                Map.of(),
                EXECUTION_STATUS_API.apply(executionId));
        final JobExecutionStatusV21 resultStatus = mock(JobExecutionStatusV21.class);
        when(resultStatus.getExecutionStatus()).thenReturn(JobExecutionStatusV21.ExecutionStatusEnum.EXECUTION_SUCCESS);
        when(mockClient.sendTMCGetRequest(uriGetTaskExecutionStatusExecution, JobExecutionStatusV21.class))
                .thenReturn(resultStatus);


        var execution = new ExecuteTaskExecution()
                .args(period, offset, limit)
                .client(mockClient)
                .request(new TMCPayloadRequest(
                        basicRequest.authentication(),
                        basicRequest.endpoint(),
                        new TMCPayload(Map.of(), body)
                ));

        assertThrows(TMCTaskExecutionDetachException.class, execution::execute);
    }

}
