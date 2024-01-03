package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFileEndpointClient implements RSEndpointClient {

    static final String LOCAL_FILE_NAME = "localfileorder.json";

    private static final LocalFileEndpointClient INSTANCE = new LocalFileEndpointClient();

    public static LocalFileEndpointClient getInstance() {
        return INSTANCE;
    }

    private LocalFileEndpointClient() {}

    @Override
    public String requestWatersEndpoint(@Nonnull String body, @Nonnull String bearerToken) throws ReportStreamEndpointClientException {
        var fileLocation = Paths.get(LOCAL_FILE_NAME);
        try {
            Files.writeString(fileLocation, body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ReportStreamEndpointClientException("Error writing the lab order", e);
        }
        return "{ \"submissionId\": \"1234567890\" }";
    }

    @Override
    public String requestHistoryEndpoint(@Nonnull String submissionId, @Nonnull String bearerToken) throws ReportStreamEndpointClientException {
        return null;
    }

    @Override
    public String getRsToken() throws ReportStreamEndpointClientException {
        return "token";
    }
}
