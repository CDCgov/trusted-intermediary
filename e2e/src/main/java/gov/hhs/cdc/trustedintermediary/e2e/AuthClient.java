package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;

public class AuthClient {
    private static final String API_ENDPOINT = "/v1/auth/token";

    public static ClassicHttpResponse authenticate(String clientId, String clientJwt)
            throws IOException {
        return HttpClient.post(
                API_ENDPOINT,
                "scope=" + clientId + "&client_assertion=" + clientJwt,
                ContentType.APPLICATION_FORM_URLENCODED,
                Map.of());
    }

    public static String requestAccessToken(String clientId, String clientJwt) throws IOException {
        try (ClassicHttpResponse response = authenticate(clientId, clientJwt)) {
            return (String) EndpointClient.getResponseBodyValue(response, "access_token");
        }
    }
}
