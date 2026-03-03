package io.camunda.connector.integration;

import io.camunda.connector.TMCTaskConnector;
import io.camunda.connector.model.*;
import org.junit.jupiter.api.BeforeEach;
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

    @Value("${tmc.environment-id}")
    private String TMC_ENV;

    @Value("${tmc.task.name}")
    private String TASK_NAME;

    @Value("${tmc.task.id}")
    private String TASK_ID;

    @Value("${tmc.task.execution-id}")
    private String EXECUTION_ID;

    private TMCAuthentication authentication;
    private TMCEndpoint endpoint;

    private final TMCTaskConnector tmcTaskConnector = new TMCTaskConnector();

    @BeforeEach
    public void setup() {
        this.authentication =
                new TMCAuthentication(
                        TMCAuthenticationType.BEARER_TOKEN.getValue(),
                        null,
                        null,
                        null,
                        PERSONAL_ACCESS_TOKEN);

        this.endpoint = new TMCEndpoint(TASK_IDENTIFIER, TMCRegionToEndpoint.Europe.getName());
    }

    @Test
    public void getTaskAPICallTest() {
        GetAvailableTaskRequest request = new GetAvailableTaskRequest(
                authentication,
                endpoint,
                new TMCPayload(Map.of(
                        "environment", TMC_ENV,
                        "name", TASK_NAME
                ), Map.of())
        );

        var result = tmcTaskConnector.getAvailableTasksRequest(request);

        assertNotNull(result);
    }

    @Test
    public void getTaskAPICallWrongCredentialsTest() {
        GetAvailableTaskRequest request = new GetAvailableTaskRequest(
                new TMCAuthentication(
                        null,
                        null,
                        null,
                        null,
                        "wrong"),
                endpoint,
                new TMCPayload(Map.of(), Map.of())
        );

        assertThrows(RuntimeException.class, () -> tmcTaskConnector.getAvailableTasksRequest(request));
    }

    @Test
    public void postExecuteTaskTest() throws Exception {
        ExecuteTaskRequest request = new ExecuteTaskRequest(
                authentication,
                endpoint,
                new TMCPayload(Map.of(), Map.of(
                        "executable", TASK_ID,
                        "parameters", Map.of(),
                        "logLevel", "INFO",
                        "timeout", "3600"

                ))
        );

        var result = tmcTaskConnector.executeTask(request, 0, 60, 100);

        assertNotNull(result);
    }

    @Test
    public void getTaskExecutionStatus() {
        GetTaskExecutionRequest request = new GetTaskExecutionRequest(
                authentication,
                endpoint
        );

        var result = tmcTaskConnector.getTaskExecutionStatus(request, EXECUTION_ID);

        assertNotNull(result);
    }

    @Test
    public void getErrorTaskExecutionStatus() {
        final String executionID = "wrong";

        GetTaskExecutionRequest request = new GetTaskExecutionRequest(
                authentication,
                endpoint
        );

        assertThrows(TMCConnectionException.class, () -> tmcTaskConnector.getTaskExecutionStatus(request, executionID));
    }

}
