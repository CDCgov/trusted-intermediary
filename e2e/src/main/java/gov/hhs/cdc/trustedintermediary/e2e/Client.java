package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import org.apache.hc.client5.http.fluent.Request;

    /** Mocks a client sending a request to the API **/
public class Client {

    private static final String DEFAULT_PROTOCOL_DOMAIN = "http://localhost:8080";
    private static String protocolDomain = DEFAULT_PROTOCOL_DOMAIN;

    public static String get(String path) throws IOException {
        System.out.println("Calling the backend at GET " + path);

        var response = Request.get(protocolDomain + path).execute();

        return response.returnContent().asString();
    }

    public static String post(String path) throws IOException {
        System.out.println("Calling the backend at POST " + path);

        var response = Request.post(protocolDomain + path).execute();

        return response.returnContent().asString();
    }
}
