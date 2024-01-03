package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.annotation.Nonnull;

public class LocalEndpointClient implements RSEndpointClient {

    static final String LOCAL_FILE_NAME = "localfileorder.json";

    private static final LocalEndpointClient INSTANCE = new LocalEndpointClient();

    public static LocalEndpointClient getInstance() {
        return INSTANCE;
    }

    private LocalEndpointClient() {}

    @Override
    public String getRsToken() {
        return "token";
    }

    @Override
    public String requestWatersEndpoint(@Nonnull String body, @Nonnull String bearerToken)
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
    public String requestHistoryEndpoint(@Nonnull String submissionId, @Nonnull String bearerToken) {
        return """
{
    "destinations" : [{
        "organization_id" : "flexion",
        "service" : "simulated-lab",
    }]
 }""";
    }
}
