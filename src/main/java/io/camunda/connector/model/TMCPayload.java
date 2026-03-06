package io.camunda.connector.model;

import java.util.Map;

public record TMCPayload(
        Map<String, Object> queryParameters,
        Map<String, Object> body
) {
    public static TMCPayload emptyPayload() {
        return new TMCPayload(Map.of(), Map.of());
    }
}
