package io.camunda.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.orchestration.PageTask;
import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.model.*;
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

@OutboundConnector(name = "Qlik TMC Connector", type = "io.camunda:cimt-qlik-tmc-outbound-connector")
@ElementTemplate(
        name = "TMC outbound connector",
        id = "io.camunda.cimt.qlik-tmc-outbound-connector.v1",
        version = 1)
public class TMCTaskConnector implements OutboundConnectorProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TMCTaskConnector.class);

    private static final String GET_TASKS_API = "/orchestration/executables/tasks";

    private final ObjectMapper mapper = new ObjectMapper();

    @Operation(id = "getTasks", name = "get available tasks")
    public PageTask getAvailableTasksRequest(@Variable GetAvailableTaskRequest request) {
        LOGGER.info("Process: Get available tasks request");

        final String bearerToken = authenticate(request.authentication());
        final URI uri = createUri(request.endpoint(), request.payload().queryParameters(), GET_TASKS_API);
        final HttpRequest tmcRequest = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + bearerToken)
                .build();

        try {
            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(tmcRequest, HttpResponse.BodyHandlers.ofString());

            LOGGER.info("Process: Get available tasks response");
            validateResponse(response);

            return mapper.readValue(response.body(), PageTask.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error processing: Get available tasks response: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error sending Request to TMC: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IllegalArgumentException(String.format("TMC request exited with status Code %s: %s", response.statusCode(), response.body()));
        }
    }

    private String authenticate(@NotNull @Valid TMCAuthentication authentication) {
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

    private URI createUri(TMCEndpoint endpoint, Map<String, String> params, String api) {
        StringBuilder url = new StringBuilder();
        url.append(TMCRegionToEndpoint.getEndpointByRegionName(endpoint.region()));
        url.append(api);
        if (!params.isEmpty()) {
            url.append("?");
            url.append(params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce("", (base, other) -> base + "&" + other));
        }
        // TODO: Sanitize?
        return URI.create(url.toString());
    }

}
