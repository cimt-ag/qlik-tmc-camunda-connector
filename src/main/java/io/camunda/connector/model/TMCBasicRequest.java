package io.camunda.connector.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TMCBasicRequest(
        @NotNull @Valid
        TMCAuthentication authentication,
        @NotNull @Valid
        TMCEndpoint endpoint
) {
}
