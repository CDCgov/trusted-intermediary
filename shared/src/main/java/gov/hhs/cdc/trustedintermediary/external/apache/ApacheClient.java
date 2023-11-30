package gov.hhs.cdc.trustedintermediary.external.apache;

import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException;
import java.io.IOException;
import java.util.Map;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.io.entity.StringEntity;

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
    public String post(String url, Map<String, String> headerMap, String body)
            throws HttpClientException {

        try {
            Request request = Request.post(url).body(new StringEntity(body));
            if (headerMap != null) {
                headerMap.forEach(request::addHeader);
            }
            return request.execute().returnContent().asString();
        } catch (IOException e) {
            throw new HttpClientException(
                    "Error occurred while making HTTP request to [" + url + "]", e);
        }
    }
}
