package io.camunda.connector.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;

public record TMCEndpoint(
        @TemplateProperty(group = "endpoint", id = "endpoint.entity")
        String entity,
        @TemplateProperty(group = "endpoint", id = "endpoint.region")
        String region
) {
}
