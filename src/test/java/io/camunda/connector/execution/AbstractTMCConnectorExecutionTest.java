package io.camunda.connector.execution;

import io.camunda.connector.AbstractTMCConnectorTest;
import io.camunda.connector.TMCHttpClient;
import io.camunda.connector.exception.TMCConnectionArgumentException;
import io.camunda.connector.exception.TMCConnectionException;
import io.camunda.connector.exception.TMCConnectorException;
import io.camunda.connector.exception.TMCErrorResponseException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractTMCConnectorExecutionTest extends AbstractTMCConnectorTest {

    protected static final String AUTH_TOKEN_MOCK = "any";

    protected final TMCHttpClient mockClient = mock(TMCHttpClient.class);

    final List<TMCConnectorException> exceptions = Arrays.asList(
            new TMCConnectionException(),
            new TMCConnectionArgumentException(""),
            new TMCErrorResponseException(""));

    protected <T, P> void testTMCGetRequestsExceptionHandling(AbstractConnectorExecution.AbstractExecutionBuilder<T, P> builder) throws TMCConnectorException {
        for (TMCConnectorException e : exceptions) {
            final TMCHttpClient client = mock(TMCHttpClient.class);

            when(client.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
            when(client.sendTMCGetRequest(any(), any())).thenThrow(e);

            assertThrows(e.getClass(), () -> builder.client(client).build().execute());
            verify(client).tmcAuthenticate(authentication);
        }
    }

    protected <T, P> void testTMCPostRequestsExceptionHandling(AbstractConnectorExecution.AbstractExecutionBuilder<T, P> builder) throws TMCConnectorException {
        for (TMCConnectorException e : exceptions) {
            final TMCHttpClient client = mock(TMCHttpClient.class);

            when(client.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
            when(client.sendTMCPostRequest(any(), any(), any())).thenThrow(e);

            assertThrows(e.getClass(), () -> builder.client(client).build().execute());
            verify(client).tmcAuthenticate(authentication);
        }
    }

    protected <T, P> void testTMCDeleteRequestsExceptionHandling(AbstractConnectorExecution.AbstractExecutionBuilder<T, P> builder) throws TMCConnectorException {
        for (TMCConnectorException e : exceptions) {
            final TMCHttpClient client = mock(TMCHttpClient.class);

            when(client.tmcAuthenticate(authentication)).thenReturn(AUTH_TOKEN_MOCK);
            doThrow(e).when(client).sendTMCDeleteRequest(any());

            assertThrows(e.getClass(), () -> builder.client(client).build().execute());
            verify(client).tmcAuthenticate(authentication);
        }
    }

}
