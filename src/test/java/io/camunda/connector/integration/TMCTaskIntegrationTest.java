package io.camunda.connector.integration;

import io.camunda.connector.TMCTaskConnector;
import io.camunda.connector.api.processing.TaskExecutionsFilters;
import io.camunda.connector.exception.TMCConnectionException;
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
    private TMCBasicRequest basicRequest;

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

        this.basicRequest = new TMCBasicRequest(
                authentication,
                endpoint
        );
    }

    @Test
    public void getTaskAPICallTest() {
        TMCPayloadRequest request = new TMCPayloadRequest(
                authentication,
                endpoint,
                new TMCPayload(Map.of(
                        "environment", TMC_ENV,
                        "name", TASK_NAME
                ), Map.of())
        );

        var result = tmcTaskConnector.getAvailableTasks(request);

        assertNotNull(result);
    }

    @Test
    public void getTaskAPICallWrongCredentialsTest() {
        TMCPayloadRequest request = new TMCPayloadRequest(
                new TMCAuthentication(
                        null,
                        null,
                        null,
                        null,
                        "wrong"),
                endpoint,
                new TMCPayload(Map.of(), Map.of())
        );

        assertThrows(RuntimeException.class, () -> tmcTaskConnector.getAvailableTasks(request));
    }

    @Test
    public void postExecuteTaskTest() throws Exception {
        TMCPayloadRequest request = new TMCPayloadRequest(
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
        var result = tmcTaskConnector.getTaskExecutionStatus(basicRequest, EXECUTION_ID);

        assertNotNull(result);
    }

    @Test
    public void getErrorTaskExecutionStatus() {
        final String executionID = "wrong";

        assertThrows(TMCConnectionException.class, () -> tmcTaskConnector.getTaskExecutionStatus(basicRequest, executionID));
    }

    @Test
    public void getAvailableTasksExecutions() {
        TMCPayloadRequest request = new TMCPayloadRequest(
                authentication,
                endpoint,
                new TMCPayload(
                        Map.of(),
                        Map.of(
                                "environmentId", TMC_ENV,
                                "status", TaskExecutionsFilters.StatusEnum.EXECUTION_SUCCESSFUL.getValue(),
                                "lastDays", 15
                        )
                )
        );

        var result = tmcTaskConnector.getAvailableTasksExecutions(request);

        assertNotNull(result);
    }

    @Test
    public void getTasksExecutionsTest() {
        TMCPayloadRequest request = new TMCPayloadRequest(
                authentication,
                endpoint,
                new TMCPayload(
                        Map.of(),
                        Map.of()
                )
        );

        var result = tmcTaskConnector.getTaskExecutions(request, TASK_ID, null);

        assertNotNull(result);
    }

}
