package gov.hhs.cdc.trustedintermediary.external.apache;

import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException;
import java.io.IOException;
import java.util.Map;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;

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
        Header[] headers = convertMapToHeader(headerMap);

        try {
            return Request.post(url)
                    .setHeaders(headers)
                    .body(new StringEntity(body))
                    .execute()
                    .returnContent()
                    .asString();
        } catch (IOException e) {
            throw new HttpClientException(
                    "Error occurred while making HTTP POST request to [" + url + "]", e);
        }
    }

    @Override
    public String get(String url, Map<String, String> headerMap) throws HttpClientException {
        Header[] headers = convertMapToHeader(headerMap);

        try {
            return Request.get(url).setHeaders(headers).execute().returnContent().asString();
        } catch (IOException e) {
            throw new HttpClientException(
                    "Error occurred while making HTTP GET request to [" + url + "]", e);
        }
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
