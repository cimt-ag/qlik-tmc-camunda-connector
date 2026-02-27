package io.camunda.connector.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;

public record TMCAuthentication(
        @TemplateProperty(group = "authentication", id = "authentication.authenticationType")
        String authenticationType,
        @TemplateProperty(group = "authentication", id = "serviceAccountId")
        String serviceAccountId,
        @TemplateProperty(group = "authentication", id = "accountSecret")
        String accountSecret,
        @TemplateProperty(group = "authentication", id = "base64")
        String base64Credentials,
        @TemplateProperty(group = "authentication", id = "bearerToken")
        String bearerToken
) {
}
