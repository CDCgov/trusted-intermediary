package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.ClientConnection;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import java.io.IOException;
import javax.inject.Inject;

public class ReportStreamConnection implements ClientConnection {

    private String trustedIntermediaryPrivatePemKey = "ENVIRONMENT_SECRET";
    private final String URL = "http://reportstream.endpoint";
    @Inject private HttpClient client;
    @Inject private AuthEngine jwt;

    private ReportStreamConnection() {}

    private static final ReportStreamConnection INSTANCE = new ReportStreamConnection();

    public static ReportStreamConnection getInstance() {
        return INSTANCE;
    }

    @Override
    public void sendRequestBody(String json, String bearerToken) {
        String res;
        try {
            res = client.post(URL, json, bearerToken); // what to do with response?
        } catch (IOException e) {
            // TODO exception handling
        }
    }

    public String requestToken() {
        // pass the key as a string: String key = new String(Files.readAllBytes(file.toPath()) for
        // local
        String senderToken = null;
        String token = "";
        try {
            senderToken = jwt.generateSenderToken("sender", "baseUrl", "pemKey", "keyId", 300);

            token = client.requestToken("reportStream.com/api-aut-endpoint", "body", senderToken);
        } catch (Exception e) {
            // TODO exception handling
        }
        return token;
    }
}
