package io.camunda.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.exception.TMCConnectionException;
import io.camunda.connector.exception.TMCErrorResponseException;
import io.camunda.connector.model.TMCAuthentication;
import io.camunda.connector.model.TMCAuthenticationType;
import io.camunda.connector.model.TMCEndpoint;
import io.camunda.connector.model.TMCRegionToEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TMCHttpClientTest {

    private TMCHttpClient client;
    private HttpClient httpClient;
    private HttpResponse<String> httpResponse;
    private TMCEndpoint endpoint = new TMCEndpoint("Task", "europe");

    static class DummyResponse {
        public String value;
    }

    @BeforeEach
    public void setup() {
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        client = new TMCHttpClient(httpClient);
    }

    private void authenticate() {
        client.tmcAuthenticate(
                new TMCAuthentication(TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        null,
                        "test-token")
        );
    }

    @Test
    public void sendTMCGetRequest_success() throws Exception {

        authenticate();

        DummyResponse dummy = new DummyResponse();
        dummy.value = "ok";

        String json = new ObjectMapper().writeValueAsString(dummy);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        DummyResponse result =
                client.sendTMCGetRequest(new URI("https://test/api"), DummyResponse.class);

        assertEquals("ok", result.value);
    }

    @Test
    public void sendTMCPostRequest_success() throws Exception {

        authenticate();

        DummyResponse dummy = new DummyResponse();
        dummy.value = "created";

        String json = new ObjectMapper().writeValueAsString(dummy);

        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.body()).thenReturn(json);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        DummyResponse result = client.sendTMCPostRequest(
                new URI("https://test/api"),
                Map.of("foo", "bar"),
                DummyResponse.class
        );

        assertEquals("created", result.value);
    }

    @Test
    public void sendTMCDeleteRequest_success() throws Exception {

        authenticate();

        when(httpResponse.statusCode()).thenReturn(204);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertDoesNotThrow(() ->
                client.sendTMCDeleteRequest(new URI("https://test/api"))
        );
    }

    @Test
    public void sendTMCRequest_errorResponse() throws Exception {

        authenticate();

        when(httpResponse.statusCode()).thenReturn(500);
        when(httpResponse.body()).thenReturn("server error");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThrows(
                TMCErrorResponseException.class,
                () -> client.sendTMCGetRequest(new URI("https://test/api"), DummyResponse.class)
        );
    }


    @Test
    public void sendTMCRequest_connectionError() throws Exception {

        authenticate();

        when(httpClient.send(any(HttpRequest.class), any()))
                .thenThrow(new IOException("connection failure"));

        assertThrows(
                TMCConnectionException.class,
                () -> client.sendTMCGetRequest(new URI("https://test/api"), DummyResponse.class)
        );
    }

    @Test
    public void tmcAuthenticate_bearerToken() {

        TMCAuthentication auth =
                new TMCAuthentication(TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        null,
                        "token");

        String token = client.tmcAuthenticate(auth);

        assertEquals("token", token);
    }

    @Test
    public void tmcAuthenticate_reuseToken() {

        TMCAuthentication auth =
                new TMCAuthentication(TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        null,
                        "token");

        client.tmcAuthenticate(auth);

        String reused = client.tmcAuthenticate(auth);

        assertEquals("token", reused);
    }

    @Test
    public void reauthenticate_shouldResetToken() {

        TMCAuthentication auth =
                new TMCAuthentication(TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        null,
                        "token");

        client.tmcAuthenticate(auth);

        String newToken = client.reauthenticate(new TMCAuthentication(TMCAuthenticationType.BEARER_TOKEN.getValue(),
                null,
                null,
                null,
                "newToken"));

        assertEquals("newToken", newToken);
    }

    @Test
    public void invalidateAuthToken_shouldClearToken() {

        TMCAuthentication auth =
                new TMCAuthentication(TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        null,
                        "token");

        client.tmcAuthenticate(auth);

        client.invalidateAuthToken();

        String newToken = client.tmcAuthenticate(auth);

        assertEquals("token", newToken);
    }

    @Test
    public void createUri_shouldBuildCorrectUri() {

        URI uri = TMCHttpClient.createUri(
                endpoint,
                Map.of("limit", 10, "offset", 5),
                TMCHttpClient.GET_TASKS_API
        );

        assertTrue(uri.toString().contains("limit=10"));
        assertTrue(uri.toString().contains("offset=5"));
        assertTrue(uri.toString().contains(TMCHttpClient.GET_TASKS_API));
    }

    @Test
    public void testUnsanitizedQueryParameters() {
        Map<String, Object> queryParameter = Map.of("un sanitized", true);

        var result = TMCHttpClient.createUri(endpoint, queryParameter, "/path");
        assertEquals(TMCRegionToEndpoint.Europe.getEndpoint()+"/path?un+sanitized=true", result.toString());
    }

}
