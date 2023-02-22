package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class ApacheClient implements HttpClient {

    private static final ApacheClient INSTANCE = new ApacheClient();
    private String token;

    private ApacheClient() {}

    public static ApacheClient getInstance() {
        return INSTANCE;
    }

    @Override
    public String post(String uri, String body) throws IOException {

        return Request.post(uri)
                .setHeader("Authorization", "Bearer" + token)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(new StringEntity(body))
                .execute()
                .toString();
    }

    public String getToken() {
        return token;
    }

    public ApacheClient setToken(String token) {
        this.token = token;
        return this;
    }
}
