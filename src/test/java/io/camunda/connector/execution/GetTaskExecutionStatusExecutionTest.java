package io.camunda.connector.execution;

import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.api.processing.JobExecutionStatusV21;
import io.camunda.connector.exception.TMCConnectorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetTaskExecutionStatusExecutionTest extends AbstractTMCConnectorExecutionTest {

    @BeforeEach
    public void setup() {
        setupRequestModels(AUTH_TOKEN_MOCK);
    }

    @Test
    public void testGetTaskExecutionStatusExecution() throws TMCConnectorException {
        final String executionId = "executionId";
        final String path = TMCHttpClient.EXECUTION_STATUS_API.apply(executionId);
        final URI uri = TMCHttpClient.createUri(basicRequest.endpoint(), Map.of(), path);

        JobExecutionStatusV21 status = mock(JobExecutionStatusV21.class);

        when(mockClient.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
        when(mockClient.sendTMCGetRequest(uri, JobExecutionStatusV21.class)).thenReturn(status);

        var result = new GetTaskExecutionStatusExecution.Builder()
                .args(executionId)
                .client(mockClient)
                .request(basicRequest)
                .build()
                .execute();

        assertEquals(status, result);
    }

    @Test
    public void testTMCHttpClientExceptions() throws TMCConnectorException {
        testTMCGetRequestsExceptionHandling(
                new GetTaskExecutionStatusExecution.Builder()
                        .args("executionId")
                        .request(basicRequest)

        );
    }

}
