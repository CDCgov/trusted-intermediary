package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.util.Map;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

/** Mocks a client sending a request to the API * */
public class HttpClient {

    private static final String DEFAULT_PROTOCOL_DOMAIN = "http://localhost:8080";
    private static String protocolDomain = DEFAULT_PROTOCOL_DOMAIN;

    public static ClassicHttpResponse get(String path, Map<String, String> headers)
            throws IOException {
        System.out.println("Calling the backend at GET " + path);
        var request = Request.get(protocolDomain + path);
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
        var response = request.execute();
        return handleResponseAndSetEntity(response);
    }

    public static ClassicHttpResponse post(
            String path, String body, ContentType type, Map<String, String> headers)
            throws IOException {
        System.out.println("Calling the backend at POST " + path);

        var request = Request.post(protocolDomain + path).bodyString(body, type);
        if (headers != null) {
            headers.forEach(request::addHeader);
        }

        var response = request.execute();
        return handleResponseAndSetEntity(response);
    }

    public static ClassicHttpResponse post(String path, String body) throws IOException {
        return post(path, body, ContentType.APPLICATION_JSON, null);
    }

    private static ClassicHttpResponse handleResponseAndSetEntity(Response response)
            throws IOException {
        var responseHandler = new ResponseHandlerWithoutException();
        var classicResponse = response.handleResponse(responseHandler);
        classicResponse.setEntity(responseHandler.getEntity());
        return classicResponse;
    }

    public static class ResponseHandlerWithoutException
            implements HttpClientResponseHandler<ClassicHttpResponse> {

        private HttpEntity entity;

        /**
         * This method implements the handleResponse method and saves the response body as an in
         * memory entity so the http connection is not kept alive and is correctly closed.
         */
        @Override
        public ClassicHttpResponse handleResponse(final ClassicHttpResponse response) {
            var contentEncoding = response.getEntity().getContentEncoding();
            var contentType = response.getEntity().getContentType();
            String entityString;
            try {
                entityString = EntityUtils.toString(response.getEntity());
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
            this.entity =
                    new StringEntity(
                            entityString, ContentType.create(contentType, contentEncoding));
            return response;
        }

        public HttpEntity getEntity() {
            return this.entity;
        }
    }
}
