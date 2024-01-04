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
                    "destinations": [{
                        "organization_id": "flexion",
                        "service": "simulated-lab"
                    }]
                 }""";
    }
}
