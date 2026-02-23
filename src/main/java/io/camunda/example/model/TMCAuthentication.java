package io.camunda.example.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;

public record TMCAuthentication(
        @TemplateProperty(group = "authentication", id = "authentication.authenticationType")
        String authenticationType,
        @TemplateProperty(group = "authentication", id = "authentication.serviceAccountId")
        String serviceAccountId,
        @TemplateProperty(group = "authentication", id = "authentication.accountSecret")
        String accountSecret,
        @TemplateProperty(group = "authentication", id = "authentication.base64Credentials")
        String base64Credentials,
        @TemplateProperty(group = "authentication", id = "authentication.bearerToken")
        String bearer
        String bearerToken,
        String authenticationType
) {
}
