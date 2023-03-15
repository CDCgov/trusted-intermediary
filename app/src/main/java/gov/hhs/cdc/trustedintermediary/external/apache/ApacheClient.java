package gov.hhs.cdc.trustedintermediary.external.apache;

import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import java.io.IOException;
import java.util.Map;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.jetbrains.annotations.NotNull;

/**
 * This class implements HttpClient and is a "humble object" for the Apache Client 5 library. Using
 * Apache Client 5 Fluent facade, we are able to perform CRUD operations such as POST, in a generic
 * way.
 */
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
                .setHeaders(headers)
                .body(new StringEntity(body))
                .execute()
                .returnResponse()
                .toString();
    }

    protected Header[] convertMapToHeader(Map<String, String> headerMap) {

        if (headerMap == null) {
            return new Header[0];
        }

        return headerMap.entrySet().stream()
                .map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                .toArray(Header[]::new);
    }
}
