package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;

public class EndpointClient {
    private String endpoint;
    private String token;

    // constructor
    public EndpointClient(String endpoint) {
        this.endpoint = endpoint;

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

    public ClassicHttpResponse submit(String fhirBody, String submissionId, boolean loginFirst)
            throws IOException {

        Map<String, String> headers = new HashMap<>();

        if (loginFirst) {
            var accessToken = AuthClient.requestAccessToken("report-stream", token);
            headers.put("Authorization", "Bearer " + accessToken);
        }
        headers.put("RecordId", submissionId);

        return HttpClient.post(endpoint, fhirBody, ContentType.APPLICATION_JSON, headers);
    }

    public static Object getResponseBodyValue(ClassicHttpResponse response, String key)
            throws IOException {
        var responseBody = JsonParsing.parseContent(response);
        return responseBody.get(key);
    }
}
