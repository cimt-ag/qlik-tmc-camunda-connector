package io.camunda.example.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;

import java.util.Map;

public record TMCPayload(
        @TemplateProperty(group = "payload", id = "payload.queryParameters")
        Map<String, String> queryParameters,
        @TemplateProperty(group = "payload", id = "payload.body")
        Map<String, String> body
) {
}
