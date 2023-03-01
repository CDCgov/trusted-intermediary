package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.ClientConnection;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import java.io.IOException;
import javax.inject.Inject;

public class ReportStreamConnection implements ClientConnection {

    private String token = "";
    private final String URI = "http://reportstream.endpoint";
    @Inject private HttpClient client;
    @Inject private AuthEngine jwt;

    private ReportStreamConnection() {}

    private static final ReportStreamConnection INSTANCE = new ReportStreamConnection();

    public static ReportStreamConnection getInstance() {
        return INSTANCE;
    }

    @Override
    public void sendRequestBody(String json) {
        String res;
        try {
            res = client.setToken(this.token).post(URI, json); // what to do with response?
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ReportStreamConnection setToken(String token) {
        this.token = token;
        return this;
    }

    public String requestToken() {
        // pass the key as a string: String key = new String(Files.readAllBytes(file.toPath()),
        // Charset.defaultCharset());
        // generate our jwt
        // GET request
        return null;
    }
}
