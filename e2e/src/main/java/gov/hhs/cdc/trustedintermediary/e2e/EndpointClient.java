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
                            Path.of("..", "mock_credentials", "report-stream-valid-token.jwt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassicHttpResponse submit(String fhirBody, boolean loginFirst) throws IOException {

        Map<String, String> headers = new HashMap<>();

        if (loginFirst) {
            var response = AuthClient.authenticate("report-stream", token);
            var parsedJsonBody = JsonParsing.parseContent(response);
            var loginToken = (String) parsedJsonBody.get("access_token");
            headers.put("Authorization", "Bearer " + loginToken);
        }

        return HttpClient.post(endpoint, fhirBody, ContentType.APPLICATION_JSON, headers);
    }
}
