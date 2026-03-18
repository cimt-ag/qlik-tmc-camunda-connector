package io.camunda.connector.model;

public record TMCAuthentication(
        String authenticationType,
        String serviceAccountId,
        String accountSecret,
        String bearerToken
) {
}