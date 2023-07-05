package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

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

    public String submit(String fhirBody) throws IOException, ParseException {
        try (var response = submitRaw(fhirBody, true)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    public ClassicHttpResponse submitRaw(String fhirBody, boolean loginFirst)
            throws IOException, ParseException {

        Map<String, String> headers = new HashMap<>();

        if (loginFirst) {
            var loginToken = AuthClient.login("report-stream", token);
            headers.put("Authorization", "Bearer " + loginToken);
        }

        return Client.post(endpoint, fhirBody, ContentType.APPLICATION_JSON, headers);
    }
}
