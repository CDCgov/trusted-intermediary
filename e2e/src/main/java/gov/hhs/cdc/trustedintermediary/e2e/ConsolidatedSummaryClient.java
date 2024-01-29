package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;

public class ConsolidatedSummaryClient {

    private final String token;

    private static final String CONSOLIDATED_SUMMARY_API_ENDPOINT = "/v1/etor/metadata/summary/";

    public ConsolidatedSummaryClient() {
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

    public ClassicHttpResponse get(String senderName, boolean loginFirst) throws IOException {
        Map<String, String> headers = new HashMap<>();

        if (loginFirst) {
            var accessToken = AuthClient.requestAccessToken("report-stream", token);
            headers.put("Authorization", "Bearer " + accessToken);
        }

        String endpointPath = CONSOLIDATED_SUMMARY_API_ENDPOINT + senderName;

        return HttpClient.get(endpointPath, headers);
    }
}
