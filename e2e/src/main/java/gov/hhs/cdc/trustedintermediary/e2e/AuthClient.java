package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class AuthClient {
    public static String login(String clientId, String clientJwt)
            throws IOException, ParseException {
        String body;
        try (ClassicHttpResponse response = loginRaw(clientId, clientJwt)) {
            body = EntityUtils.toString(response.getEntity());
        }

        var responseBody = JsonParsing.parse(body, Map.class);

        return (String) responseBody.get("access_token");
    }

    public static ClassicHttpResponse loginRaw(String clientId, String clientJwt)
            throws IOException {
        return Client.post(
                "/v1/auth",
                postBody(clientId, clientJwt),
                ContentType.APPLICATION_FORM_URLENCODED,
                Map.of());
    }

    private static String postBody(String scope, String client_assertion) {
        return "scope=" + scope + "&client_assertion=" + client_assertion;
    }
}
