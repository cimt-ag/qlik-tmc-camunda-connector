package io.camunda.connector.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TMCPayloadRequest(
        @NotNull(message = "Please define authentication credentials") @Valid
        TMCAuthentication authentication,
        @NotNull(message = "Please define the endpoint properties") @Valid
        TMCEndpoint endpoint,
        @Valid
        TMCPayload payload
) {
    public TMCBasicRequest createBasicRequest() {
        return new TMCBasicRequest(
                authentication,
                endpoint
        );
    }
}
