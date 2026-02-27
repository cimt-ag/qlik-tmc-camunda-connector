package io.camunda.connector.integration;

import io.camunda.connector.TMCTaskConnector;
import io.camunda.connector.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TMCTaskIntegrationTest {

    private static final String TASK_IDENTIFIER = "task";

    @Value("${tmc.access-token}")
    private String PERSONAL_ACCESS_TOKEN;

    private final TMCTaskConnector tmcTaskConnector = new TMCTaskConnector();

    @Test
    public void getTaskAPICallTest() {
        TMCAuthentication authentification =
                new TMCAuthentication(
                        TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        null,
                        PERSONAL_ACCESS_TOKEN);

        GetAvailableTaskRequest request = new GetAvailableTaskRequest(
                authentification,
                new TMCEndpoint(TASK_IDENTIFIER, TMCRegionToEndpoint.Europe.getName()),
                new TMCPayload(Map.of(), Map.of())
        );

        var result = tmcTaskConnector.getAvailableTasksRequest(request);

        assertNotNull(result);
    }

    @Test
    public void getTaskAPICallWrongCredentialsTest() {
        TMCAuthentication authentification =
                new TMCAuthentication(
                        null,
                        null,
                        null,
                        null,
                        "wrong");

        GetAvailableTaskRequest request = new GetAvailableTaskRequest(
                authentification,
                new TMCEndpoint(TASK_IDENTIFIER, TMCRegionToEndpoint.Europe.getName()),
                new TMCPayload(Map.of(), Map.of())
        );

        assertThrows(RuntimeException.class, () -> tmcTaskConnector.getAvailableTasksRequest(request));
    }
}
