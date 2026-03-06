package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.exception.TMCConnectorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TerminateTaskExecutionTest extends AbstractTMCConnectorExecutionTest {

    @BeforeEach
    public void setup() {
        setupRequestModels(AUTH_TOKEN_MOCK);
    }

    @Test
    public void testGetTaskExecutionsExecution() throws TMCConnectorException {
        final String executionID = "executionID";
        final String path = TMCHttpClient.TERMINATE_TASK_EXECUTION_API.apply(executionID);

        final URI uri = TMCHttpClient.createUri(basicRequest.endpoint(), Map.of(), path);

        when(mockClient.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);

        doNothing().when(mockClient).sendTMCDeleteRequest(uri);

        new TerminateTaskExecution()
                .args(executionID)
                .client(mockClient)
                .request(basicRequest)
                .execute();
        verify(mockClient, times(1)).tmcAuthenticate(authentication);
        verify(mockClient, times(1)).sendTMCDeleteRequest(uri);
    }

    @Test
    public void testTMCHttpClientExceptions() throws TMCConnectorException {
        testTMCDeleteRequestsExceptionHandling(
                new TerminateTaskExecution()
                        .request(basicRequest)
        );
    }

}
