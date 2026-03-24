package io.camunda.connector;

import io.camunda.connector.model.*;

import java.util.Map;

public class AbstractTMCConnectorTest {

    protected static final String TASK_IDENTIFIER = "task";

    protected TMCHttpAuthentication authentication;
    protected TMCAuthentication tmcAuthentication;
    protected TMCEndpoint endpoint;
    protected TMCBasicRequest basicRequest;

    protected void setupRequestModels(String personalAccessToken) {
        this.authentication =
                new TMCHttpAuthentication(
                        TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        personalAccessToken,
                        TMCRegionToEndpoint.Europe.getEndpoint());

        this.tmcAuthentication = new TMCAuthentication(
                TMCAuthenticationType.BEARER_TOKEN.getValue(),
                null,
                null,
                personalAccessToken);

        this.endpoint = new TMCEndpoint(TASK_IDENTIFIER, TMCRegionToEndpoint.Europe.getName());

        this.basicRequest = new TMCBasicRequest(
                tmcAuthentication,
                endpoint
        );
    }

    protected TMCPayloadRequest createTMCPayloadRequest(Map<String, Object> queryParams, Map<String, Object> payload) {
        return new TMCPayloadRequest(
                tmcAuthentication,
                endpoint,
                new TMCPayload(queryParams, payload)
        );
    }

    protected TMCPayloadRequest createEmptyTMCPayloadRequest() {
        return new TMCPayloadRequest(
                basicRequest.authentication(),
                basicRequest.endpoint(),
                TMCPayload.emptyPayload()
        );
    }
}
