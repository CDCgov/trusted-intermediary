package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;
import java.util.Map;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.jetbrains.annotations.NotNull;

public class ApacheClient implements HttpClient {

    private static final ApacheClient INSTANCE = new ApacheClient();

    private ApacheClient() {}

    public static ApacheClient getInstance() {
        return INSTANCE;
    }

    @Override
    public String post(
            @NotNull String url, @NotNull Map<String, String> headerMap, @NotNull String body)
            throws IOException {
        Header[] headers = convertMapToHeader(headerMap);

        return Request.post(url)
                // .setHeader("Authorization", "Bearer" + bearerToken)
                // .setHeader("client", "flexion")
                // .setHeader(HttpHeaders.CONTENT_TYPE, "application/hl7-v2") // params for headers?
                .setHeaders(headers)
                .body(new StringEntity(body))
                .execute()
                .toString();
    }

    public String requestToken(
            @NotNull String url, @NotNull Map<String, String> headerMap, @NotNull String body)
            throws IOException {
        Header[] headers = convertMapToHeader(headerMap);
        return Request.get(url)
                // .setHeader("Content-Type", "application/x-www-form-urlencoded") // Map param for
                // header?
                .setHeaders(headers)
                .body(new StringEntity(body))
                .execute()
                .toString();
    }

    protected Header[] convertMapToHeader(Map<String, String> headerMap) {

        Header[] headers;
        int index = 0;

        if (headerMap.isEmpty() | headerMap == null) {
            headers = new Header[0];
            headers[0] = new BasicHeader("", "");
            return headers;
        }

        headers = new Header[headerMap.size()];
        for (var entry : headerMap.entrySet()) {
            headers[index++] = new BasicHeader(entry.getKey(), entry.getValue());
        }
        return headers;
    }
}
