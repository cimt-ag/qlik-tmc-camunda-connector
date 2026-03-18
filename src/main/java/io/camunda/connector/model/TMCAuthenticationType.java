package io.camunda.connector.model;

public enum TMCAuthenticationType {
    BEARER_TOKEN("bearerToken"),
    CREDENTIALS_SET("credentialsSet");

    private final String value;

    TMCAuthenticationType(String value) {
        this.value = value;
    }

    public static TMCAuthenticationType valueFrom(String value) {
        if(value == null) return null;

        return switch (value) {
            case "bearerToken" -> BEARER_TOKEN;
            case "credentialsSet" -> CREDENTIALS_SET;
            default -> null;
        };
    }

    public String getValue() {
        return value;
    }
}
