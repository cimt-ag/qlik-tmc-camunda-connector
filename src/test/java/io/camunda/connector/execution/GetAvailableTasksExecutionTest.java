package io.camunda.connector.execution;

import io.camunda.connector.api.orchestration.PageTask;
import io.camunda.connector.exception.TMCConnectorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class GetAvailableTasksExecutionTest extends AbstractTMCConnectorExecutionTest {

    @BeforeEach
    public void setup() {
        setupRequestModels(AUTH_TOKEN_MOCK);
    }

    @Test
    public void testGetAvailableTaskExecution() throws TMCConnectorException {
        PageTask pageTask = mock(PageTask.class);

        when(mockClient.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
        when(mockClient.sendTMCGetRequest(any(), eq(PageTask.class))).thenReturn(pageTask);

        var result = new GetAvailableTasksExecution()
                .client(mockClient)
                .request(createEmptyTMCPayloadRequest())
                .execute();

        assertEquals(pageTask, result );
    }

    @Test
    public void testTMCHttpClientExceptions() throws TMCConnectorException {
        testTMCGetRequestsExceptionHandling(
                new GetAvailableTasksExecution()
                        .request(createEmptyTMCPayloadRequest())
        );
    }

}
