package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.ClientConnection;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import java.io.IOException;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class ReportStreamConnection implements ClientConnection {

    private String trustedIntermediaryPrivatePemKey = "ENVIRONMENT_SECRET";
    private final String URL = "http://reportstream.endpoint";
    @Inject private HttpClient client;
    @Inject private AuthEngine jwt;
    @Inject private Formatter jackson;

    private ReportStreamConnection() {}

    private static final ReportStreamConnection INSTANCE = new ReportStreamConnection();

    public static ReportStreamConnection getInstance() {
        return INSTANCE;
    }

    @Override
    public void sendRequestBody(@NotNull String json, @NotNull String bearerToken) {
        String res;
        try {
            res = client.post(URL, json, bearerToken); // what to do with response?
        } catch (IOException e) {
            // TODO exception handling
        }
    }

    public String requestToken() {
        String senderToken = null;
        String token = "";
        String body;
        try {
            senderToken = jwt.generateSenderToken("sender", "baseUrl", "pemKey", "keyId", 300);
            body = composeRequestBody(senderToken);
            String rsResponse = client.requestToken("rs endpoint", "body", senderToken);
            token = extractToken(rsResponse);
        } catch (Exception e) {
            // TODO exception handling
        }
        return token;
    }

    protected String extractToken(String responseBody) {
        String key = "access_token";
        String value = jackson.extractValueFromString(responseBody, key);
        return value;
    }

    protected String composeRequestBody(String senderToken) {
        return "fail";
    }
}
