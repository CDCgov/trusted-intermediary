package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.util.Map;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

/** Mocks a client sending a request to the API * */
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

    public static String post(String path, Map<String, String> headers, String body)
            throws IOException {
        System.out.println("Calling the backend at POST " + path);

        Header destination = new BasicHeader("Destination", headers.get("Destination"));
        Header client = new BasicHeader("Client", headers.get("Client"));
        var response =
                Request.post(protocolDomain + path)
                        .addHeader(destination)
                        .addHeader(client)
                        .bodyString(body, ContentType.APPLICATION_JSON)
                        .execute();

        return response.returnContent().asString();
    }
}
