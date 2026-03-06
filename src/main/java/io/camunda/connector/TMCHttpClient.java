package io.camunda.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.connector.exception.TMCConnectionException;
import io.camunda.connector.exception.TMCConnectorProcessingException;
import io.camunda.connector.exception.TMCErrorResponseException;
import io.camunda.connector.model.TMCAuthentication;
import io.camunda.connector.model.TMCAuthenticationType;
import io.camunda.connector.model.TMCEndpoint;
import io.camunda.connector.model.TMCRegionToEndpoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class TMCHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TMCHttpClient.class.getName());

    public static final String GET_TASKS_API = "/orchestration/executables/tasks";
    public static final String EXECUTE_TASK_API = "/processing/executions";
    public static final String AVAILABLE_TASKS_EXECUTIONS_API = "/processing/executables/tasks/executions";
    public static final Function<String, String> EXECUTION_STATUS_API =
            (String executionId) -> String.format("/processing/executions/%s", executionId);
    public static final Function<String, String> TASK_EXECUTIONS_API =
            (String taskId) -> String.format("/processing/executables/tasks/%s/executions", taskId);
    public static final Function<String, String> TERMINATE_TASK_EXECUTION_API =
            (String executionId) -> String.format("/processing/executions/%s", executionId);
    public static final Function<String, String> GET_TASK_BY_ID_API =
            (String taskId) -> String.format("/orchestration/executables/tasks/%s", taskId);

    private final ObjectMapper mapper;
    private String authToken;

    private final HttpClient client;

    public TMCHttpClient() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.client = HttpClient.newBuilder().build();
    }

    public TMCHttpClient(HttpClient client) {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.client = client;
    }

    /**
     * Sends a GET Request to the TMC.
     * This method expects the client to contain valid barerToken to authenticate the request with. Use the
     * {@link #tmcAuthenticate(TMCAuthentication)} method to authenticate at the tmc and set up the TMCHttpClient
     * with a valid bearerToken.
     * This method expects an uri for the request. You can use {@link #createUri(TMCEndpoint, Map, String)} to create the uri.
     *
     * @param uri          Path to use for the request
     * @param responseType class definition of the return type
     * @param <T>          class of the return type
     * @return the response of the GET request mapped to the {@param responseType}
     * @throws TMCConnectorProcessingException if an error occurs handling the response
     * @throws TMCConnectionException          if an error occurs while sending the GET request
     * @throws TMCErrorResponseException       if the tmc answers with an error HTTP status code
     */
    public <T> T sendTMCGetRequest(URI uri, Class<T> responseType)
            throws TMCConnectorProcessingException, TMCConnectionException, TMCErrorResponseException {
        return sendTMCRequest(
                HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + this.authToken)
                        .build(),
                responseType);
    }

    /**
     * Sends a POST Request to the TMC.
     * This method expects the client to contain valid barerToken to authenticate the request with. Use the
     * {@link #tmcAuthenticate(TMCAuthentication)} method to authenticate at the tmc and set up the TMCHttpClient
     * with a valid bearerToken.
     * This method expects an uri for the request. You can use {@link #createUri(TMCEndpoint, Map, String)} to create the uri.
     *
     * @param uri          Path to use for the request
     * @param payload      body of the POST request
     * @param responseType class definition of the return type
     * @param <T>          class of the return type
     * @return the response of the POST request mapped to the {@param responseType}
     * @throws TMCConnectorProcessingException if an error occurs handling the response
     * @throws TMCConnectionException          if an error occurs while sending the POST request
     * @throws TMCErrorResponseException       if the tmc answers with an error HTTP status code
     */
    public <T> T sendTMCPostRequest(URI uri, Map<String, Object> payload, Class<T> responseType)
            throws TMCConnectorProcessingException, TMCConnectionException, TMCErrorResponseException {
        String requestBody;

        try {
            requestBody = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error parsing body variables from connector input: {}", e.getMessage());
            throw new TMCConnectorProcessingException(e.getMessage());
        }

        return sendTMCRequest(
                HttpRequest.newBuilder()
                        .uri(uri)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + this.authToken)
                        .build(),
                responseType);
    }

    /**
     * Sends a DELETE Request to the TMC.
     * This method expects the client to contain valid barerToken to authenticate the request with. Use the
     * {@link #tmcAuthenticate(TMCAuthentication)} method to authenticate at the tmc and set up the TMCHttpClient
     * with a valid bearerToken.
     * This method expects an uri for the request. You can use {@link #createUri(TMCEndpoint, Map, String)} to create the uri.
     *
     * @param uri Path to use for the request
     * @throws TMCConnectorProcessingException if an error occurs handling the response
     * @throws TMCConnectionException          if an error occurs while sending the DELETE request
     * @throws TMCErrorResponseException       if the tmc answers with an error HTTP status code
     */
    public void sendTMCDeleteRequest(URI uri)
            throws TMCConnectionException, TMCConnectorProcessingException, TMCErrorResponseException {
        sendTMCRequest(
                HttpRequest.newBuilder()
                        .uri(uri)
                        .DELETE()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + this.authToken)
                        .build(),
                Void.class);
    }

    private <T> T sendTMCRequest(HttpRequest tmcRequest, Class<T> clazz)
            throws TMCConnectorProcessingException, TMCConnectionException, TMCErrorResponseException {
        final HttpResponse<String> response;

        try {
            response = client.send(tmcRequest, HttpResponse.BodyHandlers.ofString());

            LOGGER.debug("TMC Response: {}", response);

            validateResponse(response);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error sending Request to TMC: {}", e.getMessage());
            throw new TMCConnectionException("Error sending Request to TMC", e);
        }

        if (clazz == Void.class) {
            LOGGER.debug("No response Mapping because Void Datatype");
            return null;
        }

        try {
            LOGGER.debug("Map Response to clazz - {}", clazz.getSimpleName());
            return mapper.readValue(response.body(), clazz);
        } catch (JsonMappingException e) {
            LOGGER.error("Error mapping TMC response: {}", e.getMessage());
            throw new TMCConnectorProcessingException("Error parsing TMC response", e);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error processing TMC response: {}", e.getMessage());
            throw new TMCConnectorProcessingException("Error parsing TMC response", e);
        }
    }

    private void validateResponse(HttpResponse<String> response) throws TMCErrorResponseException {
        final int responseCode = response.statusCode();

        if (responseCode != 200 && responseCode != 201 && responseCode != 204) {
            throw new TMCErrorResponseException(String.format("TMC request exited with status Code %s: %s", response.statusCode(), response.body()));
        }
    }

    /**
     * Authenticate against the TMC and create a bearerToken for further TMC api calls.
     *
     * @param authentication parameters of the connector request
     * @return bearerToken to authenticate further TMC api calls
     */
    public String tmcAuthenticate(@NotNull @Valid TMCAuthentication authentication) {

        if (this.authToken != null && !this.authToken.isEmpty()) {
            LOGGER.debug("TMC authentication token already available - reuse token");
            return this.authToken;
        }

        TMCAuthenticationType type = TMCAuthenticationType.valueFrom(authentication.authenticationType());

        if (type == null) {
            throw new IllegalArgumentException("Authentication type is required - please provide valid credentials");
        }

        if (type == TMCAuthenticationType.BEARER_TOKEN && authentication.bearerToken() != null) {
            LOGGER.debug("Found Bearer Token in request - use bearer token for further authorization flow");
            this.authToken = authentication.bearerToken();
            return this.authToken;
        } else {
            throw new UnsupportedOperationException("Other authorizations than the bearerToken authorization are not supported yet");
        }
    }

    /**
     * invalidates the auth token with the current session is expired and a new token has to be created
     */
    public void invalidateAuthToken() {
        this.authToken = null;
    }

    /**
     * ReAuthenticate against the TMC and create a bearerToken for further TMC api calls. First invalidates current
     * authToken and then calls {@link #tmcAuthenticate(TMCAuthentication)}.
     *
     * @param authentication parameters of the connector request
     * @return bearerToken to authenticate further TMC api calls
     */
    public String reauthenticate(@NotNull @Valid TMCAuthentication authentication) {
        invalidateAuthToken();
        return tmcAuthenticate(authentication);
    }

    /**
     * Create a {@link URI} to be used to send requests against the TMC
     * Use the {@link TMCRegionToEndpoint} enum to map the region to an endpoint url.
     *
     * @param endpoint baseURL which is represented by the TMC region
     * @param params   query parameters to add to the request URI
     * @param api      part to add to the endpoint URL
     * @return full request {@link URI} with query params
     */
    public static URI createUri(TMCEndpoint endpoint, Map<String, Object> params, String api) {
        StringBuilder url = new StringBuilder();
        url.append(TMCRegionToEndpoint.getEndpointByRegionName(endpoint.region()));
        url.append(api);
        if (params != null && !params.isEmpty()) {
            url.append("?");
            url.append(params.entrySet().stream()
                    .filter(entry -> Objects.nonNull(entry.getValue()))
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce("", (base, other) -> base + "&" + other));
        }
        // TODO: Sanitize?
        return URI.create(url.toString());
    }
}
