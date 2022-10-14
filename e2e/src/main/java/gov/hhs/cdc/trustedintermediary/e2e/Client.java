package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import org.apache.hc.client5.http.fluent.Request;

public class Client {

    private static final String DEFAULT_PROTOCOL_DOMAIN = "http://localhost:8080";
    private static String protocolDomain = DEFAULT_PROTOCOL_DOMAIN;

    public static String get(String path) throws IOException {
        System.out.println("Calling the backend at " + path);

        var response = Request.get(protocolDomain + path).execute();

        return response.returnContent().asString();
    }
}
