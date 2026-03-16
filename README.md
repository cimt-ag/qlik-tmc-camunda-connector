# qlik-tmc-camunda-connector
The Qlik TMC Camunda Connector is a camunda connector to orchestrate the job execution within the Talend Management Cloud.
Therefor we combine the best of the two worlds - data pipelines with process orchestration.

## Authentication

## Task Connector Operations

This connector provides multiple operations that allow interaction with the TMC APIs.

Each operation represents a specific API action, such as retrieving available tasks, starting a task execution, or checking the status of a running execution.

When configuring the connector in a service task, the desired operation can be selected in the Endpoint section of the connector configuration.

After selecting an operation, the configuration fields (such as payload parameters) are automatically adjusted to match the requirements of the chosen operation.

This allows the connector to support multiple TMC API functionalities within a single connector while keeping the configuration simple and operation-specific.

### Get Available Tasks

Retrieve a list of available tasks from TMC based on optional query parameters.

This operation allows users to search and filter tasks and returns metadata about each available task.

- Queries available tasks from the TMC API
- Supports filtering using multiple optional query parameters
- Returns a list of tasks including metadata
- can be used to dynamically determine executable tasks in a workflow
- for more details see the [TMC API Definition](https://talend.qlik.dev/apis/orchestration/2021-03/#operation_get-available-tasks)

#### Payload

Customize the **Get Available Tasks** operation with additional _optional_ query parameters in the **Payload** section.
All parameters are optional and correspond directly to the parameters of the TMC API.

````json
{
  "environmentId": "string (optional)",
  "offset": "integer - (optional)",
  "runtimeRunProfileId": "string - (optional)",
  "runtimeType": "Enum(CLOUD, REMOTE_ENGINE, REMOTE_ENGINE_CLUSTER, CLOUD_EXCLUSIVE, PIPELINE_ENGINE) (optional)",
  "workspaceId": "string (optional)",
  "name": "string - (optional)",
  "limit": "integer - (optional)",
  "runtimeId": "string - (optional)",
  "artifactId": "string - (optional)"
}
````

#### Output / Result

The Operation returns a [PageTask](https://talend.qlik.dev/apis/orchestration/2021-03/#type_pagetask) object
- contains paging metadata which can be used to retrieve large result sets in multiple requests
- contains a list of [items](https://talend.qlik.dev/apis/orchestration/2021-03/#type_taskextract) representing the available tasks
- each item has metadata describing the task:
  - **executable** - the unique task identifier 
  - **artifactId** - identifier of the artifact the task belongs to
  - **name** - name of the task
  - **workspace information**  workspace the task is located in
  - **runtime information** - runtime environment to execute the task

A common use case is retrieving the taskId of a task by its name in order to execute the task. 

You can use the following example as _Result expression_ in the output section to extract the taskId:

````FEEL
{
  taskId: items[1].executable
}
````

#### Error

If the operation fails during execution, the connector throws the following BPMN Errors which can be handled using boundary error events in the process model. 

| Error Code                     | Description / Cause                                                  |
|--------------------------------|----------------------------------------------------------------------|
| TMC_RESPONSE_ERROR             | TMC Request wasnt successful and TMC answered with error HTTP Code   |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                         |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                        |
| TMC_TECHNICAL_ERROR            | technical error                                                      |

### Execute Task

- If a retry happens the last execution of the given executable will be monitored

#### Payload

#### Error

| Error Code                     | Description / Cause                                                                                 |
|--------------------------------|-----------------------------------------------------------------------------------------------------|
| TASK_EXECUTION_FAILED          | The Task execution was not successful or detaching the monitoring due to exceeding the retry limit  |
| TMC_RESPONSE_ERROR             | TMC Request wasnt successful and TMC answered with error HTTP Code                                  |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                                                        |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                                                       |
| TMC_TECHNICAL_ERROR            | technical error                                                                                     |

### Get Task Execution Status

Retrieve the current execution status of a task using its executionId.

This operation allows you to monitor the execution of a task and retrieve additional metadata about the execution.

It can be used to obtain information such as:
- current execution status
- potential errors messages
- timestamps of the execution
- runtime information
- additional metadata related to the task execution

#### Payload

To retrieve information about the task execution the user needs to provide an executionId in the **Payload** section.
If the executionId is evaluated dynamically you can use dynamic FEEL expression.

![Payload example](documentation/get_task_execution_status_payload_example.png)

Query parameter and a request body are not necessary to provide.

#### Output

The operation returns a [JobExecutionStatus](https://talend.qlik.dev/apis/processing/2021-03/#type_jobexecutionstatusv21) object contain metadata about the execution.

The response includes information such as:
- executionId - unique identifier of the execution
- executionStatus - current status of the execution
- startTimestamp / finishTimestamp - start and end time of the execution
- triggerTimestamp - timestamp when the execution was triggered
- userId - identifier of the user who triggered the execution
- workspaceId - workspace of the artifact and task
- processing statistics
  - numberOfProcessedRows
  - numberOfRejectedRows
- error information
  - errorType
  - errorMessage

You can use the execution result to check if the execution was successful or not. For this you can use an Output Mapping as such:

![Output Mapping Example](documentation/get_task_execution_status_output_mapping_example.png)

Use a FEEL expression to check if the execution was successful in a Gateway:
````FEEL
executionStatus = "EXECUTION_SUCCESS"
````

Check the [API documentation](https://talend.qlik.dev/apis/processing/2021-03/#type_jobexecutionstatusv21) for more execution states.

#### Error

If the operation fails during execution, the connector throws the following BPMN Errors which can be handled using boundary error events in the process model.

| Error Code                     | Description / Cause                                                  |
|--------------------------------|----------------------------------------------------------------------|
| TMC_RESPONSE_ERROR             | TMC Request wasnt successful and TMC answered with error HTTP Code   |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                         |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                        |
| TMC_TECHNICAL_ERROR            | technical error                                                      |

### Get Available Tasks Executions

#### Payload

#### Error

| Error Code                     | Description / Cause                                                  |
|--------------------------------|----------------------------------------------------------------------|
| TMC_RESPONSE_ERROR             | TMC Request wasnt successful and TMC answered with error HTTP Code   |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                         |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                        |
| TMC_TECHNICAL_ERROR            | technical error                                                      |

### Get Task Executions

#### Payload

#### Error

| Error Code                     | Description / Cause                                                   |
|--------------------------------|-----------------------------------------------------------------------|
| TMC_RESPONSE_ERROR             | TMC Request wasnt successful and TMC answered with error HTTP Code    |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                          |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                         |
| TMC_TECHNICAL_ERROR            | technical error                                                       |

### Get Task by ID

#### Payload

#### Error

| Error Code                     | Description / Cause                                                  |
|--------------------------------|----------------------------------------------------------------------|
| TMC_RESPONSE_ERROR             | TMC Request wasnt successful and TMC answered with error HTTP Code   |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                         |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                        |
| TMC_TECHNICAL_ERROR            | technical error                                                      |

### Terminate Task Execution

#### Payload

#### Error

| Error Code                     | Description / Cause                                                  |
|--------------------------------|----------------------------------------------------------------------|
| TMC_RESPONSE_ERROR             | TMC Request wasnt successful and TMC answered with error HTTP Code   |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                         |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                        |
| TMC_TECHNICAL_ERROR            | technical error                                                      |

## Developer Guide - getting started

### TMC API Model

This connector is establishing a connection to the TMC. To call the TMC API the connector is using the official openAPI specification to generate the API model:
The specifications can be found here:

- [processing API spec](src/main/resources/processing-tmc-swagger.json)
- [orchestration API spec](src/main/resources/orchestration-tmc-swagger.json)

This api spec can be found at in the [official TMC documentation](https://talend.qlik.dev/apis/processing/2021-03/swagger20.json).

The API model within the maven lifecycle. If you ever need to adjust the API model, download the swagger-configuration and run ``mvn clean compile``

### application.yaml

To develop and run the connector locally you need to configure the connector. 
The following are the basic configuration you have to provide to run the connector and connect to the SaaS Camunda Platform.
The configuration are supposed to be provided in an ``application.yaml``. This ``application.yaml`` should be placed under the [test resources](src/test/resources). 
This file is included into the [.gitignore](.gitignore) to prevent leaking secrets.

````yaml
camunda:
  client:
    mode: saas
    auth:
      client-id: {{your_client_id}}
      client-secret: {{your_client_secret}}
    cloud:
      cluster-id: {{your_cluster_id}}
      region: {{your_region}}
````

To run the integration tests you can provide following additional configurations:

````yaml
tmc:
  access-token: {{your_personal_access_token}}
  environment-id: {{your_tmc_environment_id}}
  task:
    name: {{task_name}}
    id: {{task_id}}
    execution-id: {{execution_id}}
````

To see the debug logs you can provide the following configuration:

````yaml
logging:
  level:
    root: INFO
    io:
      camunda:
        connector: DEBUG
````

### Start the TMC Connector

To start the TMC Connector you need to start the [Local Connector Runtime](src/test/java/io/camunda/connector/LocalConnectorRuntime.java). This will start the Spring Boot Application.