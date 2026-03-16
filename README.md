# qlik-tmc-camunda-connector
The Qlik TMC Camunda Connector is a camunda connector to orchestrate the job execution within the Talend Management Cloud.
Therefor we combine the best of the two worlds - data pipelines with process orchestration.

## Authentication

## Task Connector Operations

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

| Error Code                     | Description / Cause                                                |
|--------------------------------|--------------------------------------------------------------------|
| TMC_REPONSE_ERROR              | TMC Request wasnt successful and TMC answered with error HTTP Code |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                       |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                      |
| TMC_TECHNICAL_ERROR            | technical error                                                    |

### Execute Task

- If a retry happens the last execution of the given executable will be monitored

#### Payload

#### Error

| Error Code                     | Description / Cause                                                                                |
|--------------------------------|----------------------------------------------------------------------------------------------------|
| TASK_EXECUTION_FAILED          | The Task execution was not successful or detaching the monitoring due to exceeding the retry limit |
| TMC_REPONSE_ERROR              | TMC Request wasnt successful and TMC answered with error HTTP Code                                 |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                                                       |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                                                      |
| TMC_TECHNICAL_ERROR            | technical error                                                                                    |

### Get Task Execution Status

#### Payload

#### Error

| Error Code                     | Description / Cause                                                |
|--------------------------------|--------------------------------------------------------------------|
| TMC_REPONSE_ERROR              | TMC Request wasnt successful and TMC answered with error HTTP Code |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                       |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                      |
| TMC_TECHNICAL_ERROR            | technical error                                                    |

### Get Available Tasks Executions

#### Payload

#### Error

| Error Code                     | Description / Cause                                                |
|--------------------------------|--------------------------------------------------------------------|
| TMC_REPONSE_ERROR              | TMC Request wasnt successful and TMC answered with error HTTP Code |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                       |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                      |
| TMC_TECHNICAL_ERROR            | technical error                                                    |

### Get Task Executions

#### Payload

#### Error

| Error Code                     | Description / Cause                                                |
|--------------------------------|--------------------------------------------------------------------|
| TMC_REPONSE_ERROR              | TMC Request wasnt successful and TMC answered with error HTTP Code |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                       |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                      |
| TMC_TECHNICAL_ERROR            | technical error                                                    |

### Get Task by ID

#### Payload

#### Error

| Error Code                     | Description / Cause                                                |
|--------------------------------|--------------------------------------------------------------------|
| TMC_REPONSE_ERROR              | TMC Request wasnt successful and TMC answered with error HTTP Code |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                       |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                      |
| TMC_TECHNICAL_ERROR            | technical error                                                    |

### Terminate Task Execution

#### Payload

#### Error

| Error Code                     | Description / Cause                                                |
|--------------------------------|--------------------------------------------------------------------|
| TMC_REPONSE_ERROR              | TMC Request wasnt successful and TMC answered with error HTTP Code |
| TMC_CONNECTION_FAILED          | Connection to the TMC failed                                       |
| TMC_CONNECTOR_PROCESSING_ERROR | technical error while processing tmc response                      |
| TMC_TECHNICAL_ERROR            | technical error                                                    |

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