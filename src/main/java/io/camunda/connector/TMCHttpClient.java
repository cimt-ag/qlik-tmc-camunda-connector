package io.camunda.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.connector.exception.TMCConnectionException;
import io.camunda.connector.exception.TMCConnectorProcessingException;
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


    private final ObjectMapper mapper;

    public TMCHttpClient() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public <T> T sendTMCGetRequest(URI uri, String bearerToken, Class<T> responseType) throws TMCConnectorProcessingException, TMCConnectionException {
        return sendTMCRequest(
                HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + bearerToken)
                        .build(),
                responseType);
    }

    public <T> T sendTMCPostRequest(URI uri, Map<String, Object> payload, String bearerToken, Class<T> responseType) throws TMCConnectorProcessingException, TMCConnectionException {
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
                        .header("Authorization", "Bearer " + bearerToken)
                        .build(),
                responseType);
    }

    public void sendTMCDeleteRequest(URI uri, String bearerToken) throws TMCConnectionException, TMCConnectorProcessingException {
        sendTMCRequest(
                HttpRequest.newBuilder()
                        .uri(uri)
                        .DELETE()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + bearerToken)
                        .build(),
                Void.class);
    }

    private <T> T sendTMCRequest(HttpRequest tmcRequest, Class<T> clazz) throws TMCConnectorProcessingException, TMCConnectionException {
        final HttpResponse<String> response;

        try {
            response = HttpClient.newBuilder()
                    .build()
                    .send(tmcRequest, HttpResponse.BodyHandlers.ofString());

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

    private void validateResponse(HttpResponse<String> response) throws TMCConnectionException {
        final int responseCode = response.statusCode();

        if (responseCode != 200 && responseCode != 201 && responseCode != 204) {
            throw new TMCConnectionException(String.format("TMC request exited with status Code %s: %s", response.statusCode(), response.body()));
        }
    }

    public String tmcAuthenticate(@NotNull @Valid TMCAuthentication authentication) {
        TMCAuthenticationType type = TMCAuthenticationType.valueFrom(authentication.authenticationType());

        if (type == null) {
            throw new IllegalArgumentException("Authentication type is required - please provide valid Credentials");
        }

        if (type == TMCAuthenticationType.BEARER_TOKEN && authentication.bearerToken() != null) {
            LOGGER.debug("Found Bearer Token in request - use bearer token for further authorization flow");
            return authentication.bearerToken();
        } else {
            throw new UnsupportedOperationException("Other Authorizations than bearerToken Authorization are not supported yet");
        }
    }

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
