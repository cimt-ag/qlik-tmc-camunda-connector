package io.camunda.example;

import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.example.model.GetAvailableTaskRequest;
import io.camunda.example.model.TMCRegionToEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@OutboundConnector(name = "Qlik TMC Connector", type = "io.camunda:cimt-qlik-connector-api:1")
@ElementTemplate(
        id = "cimt.camunda.qlik_tmc_outbound_connector.v1",
        name = "TMC outbound connector",
        version = 1,
        description = "A connector to manage and execute tasks and plans of the tmc",
        icon = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAADo0lEQVR42rWXT0SkYRzHxxh5Z2SMjDWysjKSJB0yspIkyVgdOiQdsofMsNZaHZIOS9YaK1nJSPbQYQ8rK52yhyTpsJK1kiRZzWuNpEPGGus1svv5jale77x/Zt5tho9nnmee5/f7/n7P3/FU88kkFC80Z5L+QTXpn4P3asL/jvoY7X2ZhD9IGQAf7Y723DjvxOk+3MBfE3KwgZBxyiaEKZQPJiAC62pC0cSZHURfgO98fwFPEOL7L+cqBnD+msiy4qBSGKNlkso6AnoRFHAtAAOtGNsWoy45QMAY2Qu6TL//FQYuqnCoQRZ+MHaXcotMLBFE1M3cN8CqzKuD0xsydU2Ue/RP01/mfxA6oEWgvd5N9DE1qew6zTXsEOUMAp6aOHL/wfkonFo6Z+vheJGym3qd56E/GJ2ErEXktBd3R8RTqw+RzUK+3Ln/FwLWYAOW6TNNOQLRaiPsx1irze+p8oMGQZwLOGtnbBo0BFxSP4UdSFFnLVQwJRhbwuiKzSKcgmuDgE/QiCMPNNGW1u8GxF2zLg75Pq8SnJOAfZlLGLUQkICsLvUn0IUTb2mbSpairIVtk22Zo69kZAh8VgI06UyHY+gz2QXDGDm6j16ZgaDJUd1P1Jcmi7XAb0f4iZuK0G0nEXFoFEG9S6Iopf4cusBoQwIJwZzVIQUnMAA+UwH6FGNoBEF1YhgURKyAGFmARzYLtgOszwyyTJYkAK9+UMGk4wUC3src0jlA/TmcwYTdYcPUhOkzb3dqImANm48p71J8ZnGna2TjG7ykzzDlF8pBWXyGNeSRbGGwgTJKn2lKzSYLebkZ7wKhYbOCB8ZZabfMwQD0QKxU9uJwHIEfEPGV+jbfjx1s7kEEilMw5eaOx9mVyrowXEhbEIfPTuMR2YNwrwhox1heGt2CoQJIhoYhAosVjBstPtXoHEDApmsBieL23cfYs7v3AwvYYdw5tMluuH3txjlwflfrnDT+KV1GcXFeqQDGvYF6/WkXgtUqnR/hLAVthoOrEdI2u+AAOC90Z4HMBcRk2zk4zsluoF8KhhgTMrk7WmDDwvkVTDAuYHYnKAzs58cFSlnF67fQ9pGBszAG3fRtAK/F+yEGhyYZy4Ec1WG7B6gPItAMLTqaIAheh8dLndyqOMmXR07WsKO3UYvnm4hfNkSfpW0GAY21cG6cxk74eX+UK7swgvMQ9Zo7D3MyLsmhJo4RMSGXDhgur9qlP4zzScoOCICvkn/G/wC2FuY+yUnk/QAAAABJRU5ErkJggg==",
        documentationRef =
                "https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/" // TODO: add documentation reference here
)
public class TMCTaskConnector implements OutboundConnectorProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TMCTaskConnector.class);

    private static final String GET_TASKS_API = "/orchestration/executables/tasks";

    @Operation(id = "get_tasks", name = "get available tasks")
    public Object getAvailableTasksRequest(@Variable GetAvailableTaskRequest request) {
        LOGGER.info("hier bin ich angekommen");

        // TODO: hier sollte ich eine Unterscheidung treffen, ob es sich um einen ServiceAccount Zugriff handelt oder um einen direkten Zugriff mit einem personal Access Token
        // TODO: Das muss auch noch im Template berücksichtigt werden

        final String baseUrl = TMCRegionToEndpoint.getEndpointByRegionName(request.region());
        final URI uri = URI.create(baseUrl + GET_TASKS_API);

        HttpRequest tmcRequest = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", String.format("BEARER %s", request.authentication().bearer()))
                .build();

        HttpResponse<?> tmcResponse;
        try {
            tmcResponse = HttpClient.newBuilder()
                    .build()
                    .send(tmcRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            LOGGER.error("interrupted");
            throw new RuntimeException(e);
        }

        return "Moin";
    }
}
