package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

/** Mocks a client sending a request to the API * */
public class Client {

    private static final String DEFAULT_PROTOCOL_DOMAIN = "http://localhost:8080";
    private static String protocolDomain = DEFAULT_PROTOCOL_DOMAIN;

    public static ClassicHttpResponse get(String path) throws IOException {
        System.out.println("Calling the backend at GET " + path);
        var response = Request.get(protocolDomain + path).execute();
        return response.handleResponse(new ResponseHandlerWithoutException());
    }

    public static ClassicHttpResponse post(String path, String body, ContentType type)
            throws IOException {
        System.out.println("Calling the backend at POST " + path);
        var response = Request.post(protocolDomain + path).bodyString(body, type).execute();
        return response.handleResponse(new ResponseHandlerWithoutException());
    }

    public static ClassicHttpResponse post(String path, String body) throws IOException {
        return post(path, body, ContentType.APPLICATION_JSON);
    }

    public static class ResponseHandlerWithoutException
            implements HttpClientResponseHandler<ClassicHttpResponse> {

        /**
         * Very similar code to {@link
         * AbstractHttpClientResponseHandler#handleResponse(ClassicHttpResponse)} but doesn't throw
         * an exception when getting a 4xx or 5xx status code.
         */
        @Override
        public ClassicHttpResponse handleResponse(final ClassicHttpResponse response) {
            return response;
        }
    }

    //    public static class ContentResponseHandlerWithoutException extends ContentResponseHandler
    // {
    //
    //        /**
    //         * Very similar code to {@link
    //         * AbstractHttpClientResponseHandler#handleResponse(ClassicHttpResponse)} but doesn't
    // throw
    //         * an exception when getting a 4xx or 5xx status code.
    //         */
    //        @Override
    //        public Content handleResponse(final ClassicHttpResponse response) throws IOException {
    //
    //            if (response.getCode() >= HttpStatus.SC_REDIRECTION) {
    //                HttpEntity entity = response.getEntity();
    //                if (entity == null) {
    //                    return null;
    //                }
    //                return handleEntity(entity);
    //            }
    //
    //            return super.handleResponse(response);
    //        }
    //    }
}
