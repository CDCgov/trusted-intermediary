package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * A mock implementation of the RSEndpointClient interface that doesn't require a connection to
 * ReportStream
 */
public class MockRSEndpointClient implements RSEndpointClient {

    static final String LOCAL_FILE_NAME = "localfileorder.json";

    private static final MockRSEndpointClient INSTANCE = new MockRSEndpointClient();

    public static MockRSEndpointClient getInstance() {
        return INSTANCE;
    }

    private MockRSEndpointClient() {}

    @Override
    public String getRsToken() {
        return "token";
    }

    @Override
    public String requestWatersEndpoint(String body, String bearerToken)
            throws ReportStreamEndpointClientException {
        var fileLocation = Paths.get(LOCAL_FILE_NAME);
        try {
            Files.writeString(fileLocation, body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ReportStreamEndpointClientException("Error writing the lab order", e);
        }
        return "{ \"reportId\": \"" + UUID.randomUUID() + "\" }";
    }

    @Override
    public String requestHistoryEndpoint(String submissionId, String bearerToken) {
        return """
                {
                    "timestamp" : "2020-01-01T00:00:00.000Z",
                    "sender" : "flexion.simulated-hospital",
                    "actualCompletionAt" : "2023-10-24T19:48:26.921Z",
                    "overallStatus": "Not Delivering",
                    "destinations": [{
                        "organization_id": "flexion",
                        "service": "simulated-lab"
                    }],
                    "errors": [{
                        "message": "The message was not good"
                    }, {
                        "message": "The DogCow couldn't Moof!"
                    }]
                 }""";
    }

    @Override
    public String requestDeliveryEndpoint(String reportId, String bearerToken)
            throws ReportStreamEndpointClientException {
        return """
            {
                "deliveryId": 20,
                "batchReadyAt": "2024-04-09T18:19:00.431Z",
                "expires": "2024-05-09T18:19:00.431Z",
                "receiver": "flexion.etor-service-receiver-orders",
                "receivingOrgSvcStatus": null,
                "reportId": "ddfeb4e2-af58-433e-9297-a4be01957225",
                "topic": "etor-ti",
                "reportItemCount": 2,
                "fileName": "fhir-transform-sample.yml-ddfeb4e2-af58-433e-9297-a4be01957225-20240409181900.fhir",
                "fileType": "FHIR",
                "originalIngestion": [
                    {
                        "reportId": "2f5f17e7-2161-44d9-b091-2d53c10f6e90",
                        "ingestionTime": "2024-04-09T18:17:56.571Z",
                        "sendingOrg": "DogCow Associates"
                    },
                    {
                        "reportId": "e18c283e-e2e4-4804-bca3-33afe32e6b69",
                        "ingestionTime": "2024-04-09T18:18:00.553Z",
                        "sendingOrg": "DogCow Associates"
                    }
                ]
            }
        """;
    }
}
