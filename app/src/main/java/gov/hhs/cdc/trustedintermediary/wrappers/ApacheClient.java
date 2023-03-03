package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.jetbrains.annotations.NotNull;

public class ApacheClient implements HttpClient {

    private static final ApacheClient INSTANCE = new ApacheClient();

    private ApacheClient() {}

    public static ApacheClient getInstance() {
        return INSTANCE;
    }

    @Override
    public String post(@NotNull String url, @NotNull String body, @NotNull String bearerToken)
            throws IOException {

        return Request.post(url)
                .setHeader("Authorization", "Bearer" + bearerToken)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(new StringEntity(body))
                .execute()
                .toString();
    }

    public String requestToken(@NotNull String url, @NotNull String body, @NotNull String token)
            throws IOException {

        // I think we need to include the client name in the header and probably the access scope

        return Request.get(url)
                .setHeader("Authorization", "Bearer" + token)
                .body(new StringEntity(body)) // Don't know if we need a body
                .execute()
                .toString();
    }
}
