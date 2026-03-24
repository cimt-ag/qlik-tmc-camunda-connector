package io.camunda.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.oauth.TokenResponse;
import io.camunda.connector.exception.TMCAuthenticationException;
import io.camunda.connector.exception.TMCConnectionArgumentException;
import io.camunda.connector.exception.TMCConnectionException;
import io.camunda.connector.exception.TMCErrorResponseException;
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
import static org.mockito.Mockito.*;

public class TMCHttpClientTest {

    private TMCHttpClient client;
    private HttpClient httpClient;
    private HttpResponse<String> httpResponse;
    private final TMCEndpoint endpoint = new TMCEndpoint("Task", "europe");

    TMCHttpAuthentication authBearer =
            new TMCHttpAuthentication(TMCAuthenticationType.BEARER_TOKEN.getValue(),
                    null,
                    null,
                    "token",
                    TMCRegionToEndpoint.Europe.getEndpoint());

    static class DummyResponse {
        public String value;

    }

    private void authenticate() throws Exception {
        client.tmcAuthenticate(authBearer);
    }

    @BeforeEach
    public void setup() {
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        client = new TMCHttpClient(httpClient);
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
    public void tmcAuthenticate_bearerToken() throws Exception {
        String token = client.tmcAuthenticate(this.authBearer);

        assertEquals("token", token);
    }

    @Test
    public void tmcAuthenticate_reuseToken() throws Exception {
        authenticate();
        String reused = client.tmcAuthenticate(authBearer);

        assertEquals("token", reused);
    }

    @Test
    public void reauthenticate_shouldResetToken() throws Exception {
        authenticate();

        String newToken = client.reauthenticate(new TMCHttpAuthentication(TMCAuthenticationType.BEARER_TOKEN.getValue(),
                null,
                null,
                "newToken",
                TMCRegionToEndpoint.Europe.getEndpoint()));

        assertEquals("newToken", newToken);
    }

    @Test
    public void invalidateAuthToken_shouldClearToken() throws Exception {
        authenticate();
        client.invalidateAuthToken();

        String newToken = client.tmcAuthenticate(authBearer);

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
        assertEquals(TMCRegionToEndpoint.Europe.getEndpoint() + "/path?un+sanitized=true", result.toString());
    }

    @Test
    public void TMC400StatusCode() throws Exception {
        authenticate();

        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("server error");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThrows(
                TMCConnectionArgumentException.class,
                () -> client.sendTMCGetRequest(new URI("https://test/api"), DummyResponse.class)
        );
    }

    @Test
    public void TMCServiceAccountCredentialSetFlow() throws Exception {

        final String token = "token";

        TokenResponse response = new TokenResponse();
        response.setAccessToken(token);
        response.expiresIn("100");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        when(httpResponse.body()).thenReturn(new ObjectMapper().writeValueAsString(response));
        when(httpResponse.statusCode()).thenReturn(200);

        TMCHttpAuthentication serviceAccountAuth = new TMCHttpAuthentication(
                TMCAuthenticationType.CREDENTIALS_SET.getValue(),
                "client-id",
                "secret",
                null,
                TMCRegionToEndpoint.Europe.getEndpoint()
        );

        String result = client.tmcAuthenticate(serviceAccountAuth);

        //reuse token
        String reuse = client.tmcAuthenticate(serviceAccountAuth);

        assertEquals(token, result);
        assertEquals(token, reuse);

        verify(httpClient, times(1)).send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void TMCServiceAccountExpiredCredentialSetFlow() throws Exception {

        final String token = "token";

        TokenResponse response = new TokenResponse();
        response.setAccessToken(token);
        response.expiresIn("0");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        when(httpResponse.body()).thenReturn(new ObjectMapper().writeValueAsString(response));
        when(httpResponse.statusCode()).thenReturn(200);

        TMCHttpAuthentication serviceAccountAuth = new TMCHttpAuthentication(
                TMCAuthenticationType.CREDENTIALS_SET.getValue(),
                "client-id",
                "secret",
                null,
                TMCRegionToEndpoint.Europe.getEndpoint()
        );

        String result = client.tmcAuthenticate(serviceAccountAuth);

        //reuse token
        String reuse = client.tmcAuthenticate(serviceAccountAuth);

        assertEquals(token, result);
        assertEquals(token, reuse);

        verify(httpClient, times(2)).send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void TMCServiceAccountCredentialSetFlowUnauthorized() throws Exception {

        final String token = "token";

        TokenResponse response = new TokenResponse();
        response.setAccessToken(token);
        response.expiresIn("0");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        when(httpResponse.body()).thenReturn(new ObjectMapper().writeValueAsString(response));
        when(httpResponse.statusCode()).thenReturn(401);

        TMCHttpAuthentication serviceAccountAuth = new TMCHttpAuthentication(
                TMCAuthenticationType.CREDENTIALS_SET.getValue(),
                "client-id",
                "secret",
                null,
                TMCRegionToEndpoint.Europe.getEndpoint()
        );

        assertThrows(TMCAuthenticationException.class, () -> client.tmcAuthenticate(serviceAccountAuth));
    }

}
