# qlik-tmc-camunda-connector
The Qlik TMC Camunda Connector is a camunda connector to orchestrate the job execution within the Talend Management Cloud.
Therefor we combine the best of the two worlds - data pipelines with process orchestration.

## Connector Operations

### Execute Task

- If a retry happens the last execution of the given executable will be monitored
- Returns following Error Codes:

| Error Code            | Description                                                                                 |
|-----------------------|---------------------------------------------------------------------------------------------|
| TASK_EXECUTION_FAILED | The Task execution is not successful or the connector detaches due to exceding retry limits |


## TMC API Model

This connector is establishing a connection to the TMC. To call the TMC API the connector is using the official openAPI specification to generate the API model:
The specifications can be found here:

- [processing API spec](src/main/resources/processing-tmc-swagger.json)
- [orchestration API spec](src/main/resources/orchestration-tmc-swagger.json)

This api spec can be found at in the [official TMC documentation](https://talend.qlik.dev/apis/processing/2021-03/swagger20.json).

The API model within the maven lifecycle. If you ever need to adjust the API model, download the swagger-configuration and run ``mvn clean compile``

