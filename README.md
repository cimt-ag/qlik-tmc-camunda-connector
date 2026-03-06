# qlik-tmc-camunda-connector
The Qlik TMC Camunda Connector is a camunda connector to orchestrate the job execution within the Talend Management Cloud.
Therefor we combine the best of the two worlds - data pipelines with process orchestration.

## Authentication

## Task Connector Operations

### Get Available Tasks

- Search for tasks based on query parameter
- With this operation you can query the taskId (parameter: executable) 
- returns a list of tasks extracts with metadata about the task
- get more Information about the API Call from the [TMC API Definition](https://talend.qlik.dev/apis/orchestration/2021-03/#operation_get-available-tasks)

#### Payload
Use these as _query parameter_ in the payload group
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

#### Error

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

