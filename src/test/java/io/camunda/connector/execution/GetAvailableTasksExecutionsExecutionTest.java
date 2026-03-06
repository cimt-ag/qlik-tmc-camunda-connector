package io.camunda.connector.execution;

import io.camunda.connector.api.processing.PageTaskExecutionStatus;
import io.camunda.connector.exception.TMCConnectorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class GetAvailableTasksExecutionsExecutionTest extends AbstractTMCConnectorExecutionTest {

    @BeforeEach
    public void setup() {
        setupRequestModels(AUTH_TOKEN_MOCK);
    }

    @Test
    public void testGetAvailableTasksExecutionsExecution() throws TMCConnectorException {
        PageTaskExecutionStatus status = mock(PageTaskExecutionStatus.class);

        when(mockClient.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
        when(mockClient.sendTMCPostRequest(any(), any(), eq(PageTaskExecutionStatus.class))).thenReturn(status);

        var result = new GetAvailableTasksExecutionsExecution()
                .client(mockClient)
                .request(createEmptyTMCPayloadRequest())
                .execute();

        assertEquals(status, result);
    }

    @Test
    public void testTMCHttpClientExceptions() throws TMCConnectorException {
        testTMCPostRequestsExceptionHandling(
                new GetAvailableTasksExecutionsExecution()
                        .request(createEmptyTMCPayloadRequest())
        );
    }

}
