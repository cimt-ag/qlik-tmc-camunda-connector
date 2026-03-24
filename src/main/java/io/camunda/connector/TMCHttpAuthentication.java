package io.camunda.connector;

import io.camunda.connector.model.*;

public record TMCHttpAuthentication(
        String authenticationType,
        String serviceAccountId,
        String accountSecret,
        String bearerToken,
        String tmcEndpoint
) {
    public static TMCHttpAuthentication create(TMCBasicRequest request) {
        return create(request.endpoint(), request.authentication());
    }

    public static TMCHttpAuthentication create(TMCPayloadRequest request) {
        return create(request.endpoint(), request.authentication());
    }

    private static TMCHttpAuthentication create(TMCEndpoint endpoint, TMCAuthentication authentication) {
        return new TMCHttpAuthentication(
                authentication.authenticationType(),
                authentication.serviceAccountId(),
                authentication.accountSecret(),
                authentication.bearerToken(),
                TMCRegionToEndpoint.getEndpointByRegionName(endpoint.region())
        );
    }
}
