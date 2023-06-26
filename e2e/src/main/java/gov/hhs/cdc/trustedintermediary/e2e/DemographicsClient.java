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

public class DemographicsClient {

    private static final String CLIENT_TOKEN;
    private static final String API_ENDPOINT = "/v1/etor/demographics";

    static {
        try {
            CLIENT_TOKEN =
                    Files.readString(
                            Path.of("..", "mock_credentials", "report-stream-valid-token.jwt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String submitDemographics(String fhirBody) throws IOException, ParseException {
        try (var response = submitDemographicsRaw(fhirBody, true)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    public static ClassicHttpResponse submitDemographicsRaw(String fhirBody, boolean loginFirst)
            throws IOException, ParseException {

        Map<String, String> headers = new HashMap<>();

        if (loginFirst) {
            var loginToken = AuthClient.login("report-stream", CLIENT_TOKEN);
            headers.put("Authorization", "Bearer " + loginToken);
        }

        return Client.post(API_ENDPOINT, fhirBody, ContentType.APPLICATION_JSON, headers);
    }
}
