package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        return "{ \"submissionId\": \"1234567890\" }";
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
}
