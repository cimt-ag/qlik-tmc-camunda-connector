package io.camunda.example.model;

import java.util.Arrays;

public enum TMCRegionToEndpoint {
    europe("europe", "https://api.eu.cloud.talend.com"),
    pacific("pacific", "https://api.ap.cloud.talend.com"),
    us_east("us_east", "https://api.us.cloud.talend.com"),
    us_west("us_west", "https://api.us-west.cloud.talend.com"),
    australia("australia","https://api.au.cloud.talend.com");

    private final String name;
    private final String endpoint;

    TMCRegionToEndpoint(String name, String endpoint) {
        this.name = name;
        this.endpoint = endpoint;
    }

    public static String getEndpointByRegionName(String region) {
        return switch (region) {
            case "europe" -> europe.endpoint;
            case "pacific" -> pacific.endpoint;
            case "us_east" -> us_east.endpoint;
            case "us_west" -> us_west.endpoint;
            case "australia" -> australia.endpoint;
            default -> null;
        };
    }

    public String toString() {
        return this.name;
    }
}
