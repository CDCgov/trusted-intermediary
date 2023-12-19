package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;

public class MetadataClient {

    private final String token;

    private static final String METADATA_ENDPOINT_PATH = "/v1/etor/metadata/";

    public MetadataClient() {
        try {
            this.token =
                    Files.readString(
                                    Path.of(
                                            "..",
                                            "mock_credentials",
                                            "report-stream-valid-token.jwt"))
                            .trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassicHttpResponse get(String submissionId, boolean loginFirst) throws IOException {
        Map<String, String> headers = new HashMap<>();

        if (loginFirst) {
            var accessToken = AuthClient.requestAccessToken("report-stream", token);
            headers.put("Authorization", "Bearer " + accessToken);
        }

        String endpointPath = METADATA_ENDPOINT_PATH + submissionId;

        return HttpClient.get(endpointPath, headers);
    }
}
