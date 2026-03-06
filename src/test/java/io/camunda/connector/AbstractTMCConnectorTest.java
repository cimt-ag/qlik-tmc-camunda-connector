package io.camunda.connector;

import io.camunda.connector.model.*;

public class AbstractTMCConnectorTest {

    protected static final String TASK_IDENTIFIER = "task";

    protected TMCAuthentication authentication;
    protected TMCEndpoint endpoint;
    protected TMCBasicRequest basicRequest;

    protected void setupRequestModels(String personalAccessToken) {
        this.authentication =
                new TMCAuthentication(
                        TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        null,
                        personalAccessToken);

        this.endpoint = new TMCEndpoint(TASK_IDENTIFIER, TMCRegionToEndpoint.Europe.getName());

        this.basicRequest = new TMCBasicRequest(
                authentication,
                endpoint
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
