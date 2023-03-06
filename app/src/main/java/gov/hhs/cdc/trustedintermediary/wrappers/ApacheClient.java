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

    public String requestToken(@NotNull String url, @NotNull String body) throws IOException {
        return Request.get(url)
                .setHeader(
                        "Content-Type",
                        "application/x-www-form-urlencoded") // should a Map<String,String> be
                // passed as a param?
                .body(new StringEntity(body))
                .execute()
                .toString();
    }
}
