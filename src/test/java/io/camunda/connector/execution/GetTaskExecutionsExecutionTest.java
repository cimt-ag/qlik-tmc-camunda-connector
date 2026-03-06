package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.PageTaskExecutionStatus;
import io.camunda.connector.exception.TMCConnectorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetTaskExecutionsExecutionTest extends AbstractTMCConnectorExecutionTest {

    @BeforeEach
    public void setup() {
        setupRequestModels(AUTH_TOKEN_MOCK);
    }

    @Test
    public void testGetTaskExecutionsExecution() throws TMCConnectorException {
        final String taskId = "taskId";
        final String path = TMCHttpClient.TASK_EXECUTIONS_API.apply(taskId);
        final URI uri = TMCHttpClient.createUri(basicRequest.endpoint(), Map.of(), path);

        PageTaskExecutionStatus status = mock(PageTaskExecutionStatus.class);

        when(mockClient.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
        when(mockClient.sendTMCGetRequest(uri, PageTaskExecutionStatus.class)).thenReturn(status);

        var result = new GetTaskExecutionsExecution()
                .args(taskId)
                .client(mockClient)
                .request(createEmptyTMCPayloadRequest())
                .execute();

        assertEquals(status, result);
    }

    @Test
    public void testTMCHttpClientExceptions() throws TMCConnectorException {
        testTMCGetRequestsExceptionHandling(
                new GetTaskExecutionsExecution()
                        .request(createEmptyTMCPayloadRequest())
        );
    }

}
