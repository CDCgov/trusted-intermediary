package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;

/** Mocks a client sending a request to the API * */
public class Client {

    private static final String DEFAULT_PROTOCOL_DOMAIN = "http://localhost:8080";
    private static String protocolDomain = DEFAULT_PROTOCOL_DOMAIN;

    public static ClassicHttpResponse get(String path) throws IOException {
        System.out.println("Calling the backend at GET " + path);
        var response = Request.get(protocolDomain + path).execute();
        return response.handleResponse(new ResponseHandlerWithoutException());
    }

    public static OurClassicHttpResponse post(String path, String body, ContentType type)
            throws IOException {
        System.out.println("Calling the backend at POST " + path);
        var response = Request.post(protocolDomain + path).bodyString(body, type).execute();
        return (OurClassicHttpResponse)
                response.handleResponse(new ResponseHandlerWithoutException());
    }

    public static OurClassicHttpResponse post(String path, String body) throws IOException {
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
        public OurClassicHttpResponse handleResponse(final ClassicHttpResponse response) {
            OurClassicHttpResponse ourResponse = (OurClassicHttpResponse) response;
            String body;
            try {
                body = EntityUtils.toString(response.getEntity());
            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }
            ourResponse.setBody(body);
            return ourResponse;
        }
    }

    public static class OurClassicHttpResponse extends BasicClassicHttpResponse {

        private String body;

        public OurClassicHttpResponse(int code) {
            super(code);
        }

        public String getBody() {
            return this.body;
        }

        public void setBody(String body) {
            this.body = body;
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
