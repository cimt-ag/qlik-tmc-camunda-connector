package io.camunda.connector.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;

import java.util.Map;

public record TMCPayload(
        @TemplateProperty(group = "payload", id = "payload.queryParameters")
        Map<String, Object> queryParameters,
        @TemplateProperty(group = "payload", id = "payload.body")
        Map<String, Object> body
) {
        public static TMCPayload emptyPayload() {
                return new TMCPayload(Map.of(), Map.of());
        }
}
