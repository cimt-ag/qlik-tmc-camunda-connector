package io.camunda.connector.model;

public enum TMCRegionToEndpoint {
    Europe("europe", "https://api.eu.cloud.talend.com"),
    PACIFIC("pacific", "https://api.ap.cloud.talend.com"),
    US_EAST("us_east", "https://api.us.cloud.talend.com"),
    US_WEST("us_west", "https://api.us-west.cloud.talend.com"),
    AUSTRALIA("australia", "https://api.au.cloud.talend.com");

    private final String name;
    private final String endpoint;

    TMCRegionToEndpoint(String name, String endpoint) {
        this.name = name;
        this.endpoint = endpoint;
    }

    public static String getEndpointByRegionName(String region) {
        return switch (region) {
            case "europe" -> Europe.endpoint;
            case "pacific" -> PACIFIC.endpoint;
            case "us_east" -> US_EAST.endpoint;
            case "us_west" -> US_WEST.endpoint;
            case "australia" -> AUSTRALIA.endpoint;
            default -> null;
        };
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return this.name;
    }
}
