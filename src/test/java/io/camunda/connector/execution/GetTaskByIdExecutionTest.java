package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.orchestration.TaskV21;
import io.camunda.connector.exception.TMCConnectorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetTaskByIdExecutionTest extends AbstractTMCConnectorExecutionTest {

    @BeforeEach
    public void setup() {
        setupRequestModels(AUTH_TOKEN_MOCK);
    }

    @Test
    public void testGetTaskByIdExecution() throws TMCConnectorException {
        final String taskId = "taskId";
        final String path = TMCHttpClient.GET_TASK_BY_ID_API.apply(taskId);
        final URI uri = TMCHttpClient.createUri(basicRequest.endpoint(), Map.of(), path);

        TaskV21 task = mock(TaskV21.class);

        when(mockClient.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
        when(mockClient.sendTMCGetRequest(uri, TaskV21.class)).thenReturn(task);

        var result = new GetTaskByIdExecution.Builder()
                .args(taskId)
                .client(mockClient)
                .request(basicRequest)
                .build()
                .execute();

        assertEquals(task, result);
    }

    @Test
    public void testTMCHttpClientExceptions() throws TMCConnectorException {
        testTMCGetRequestsExceptionHandling(
                new GetTaskByIdExecution.Builder()
                        .args("taskId")
                        .request(basicRequest)
        );
    }

}
