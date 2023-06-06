package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.ContentResponseHandler;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.*;

/** Mocks a client sending a request to the API * */
public class Client {

    private static final String DEFAULT_PROTOCOL_DOMAIN = "http://localhost:8080";
    private static String protocolDomain = DEFAULT_PROTOCOL_DOMAIN;

    public static String get(String path) throws IOException {
        System.out.println("Calling the backend at GET " + path);

        var response = Request.get(protocolDomain + path).execute();

        return response.handleResponse(new ContentResponseHandlerWithoutException()).asString();
    }

    public static String post(String path, String body) throws IOException {
        return post(path, body, ContentType.APPLICATION_JSON);
    }

    public static String post(String path, String body, ContentType type) throws IOException {
        System.out.println("Calling the backend at POST " + path);

        return requestPost(path, body, type)
                .handleResponse(new ContentResponseHandlerWithoutException())
                .asString();
    }

    public static Response requestPost(String path, String body, ContentType type)
            throws IOException {
        return Request.post(protocolDomain + path).bodyString(body, type).execute();
    }

    public static class ContentResponseHandlerWithoutException extends ContentResponseHandler {

        /**
         * Very similar code to {@link
         * AbstractHttpClientResponseHandler#handleResponse(ClassicHttpResponse)} but doesn't throw
         * an exception when getting a 4xx or 5xx status code.
         */
        @Override
        public Content handleResponse(final ClassicHttpResponse response) throws IOException {

            if (response.getCode() >= HttpStatus.SC_REDIRECTION) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }
                return handleEntity(entity);
            }

            return super.handleResponse(response);
        }
    }
}
