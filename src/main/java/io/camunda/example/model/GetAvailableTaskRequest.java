package io.camunda.example.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record GetAvailableTaskRequest(
        // Authentication
        @NotNull @Valid
        TMCAuthentication authentication,
        //Endpoint
        @TemplateProperty(group = "endpoint", id = "endpoint.region")
        String region,
        @TemplateProperty(group = "endpoint", id = "queryParameters")
        String queryParameters
) {
}
