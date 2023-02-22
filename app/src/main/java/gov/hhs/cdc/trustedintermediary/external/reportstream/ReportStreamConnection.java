package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.wrappers.ApacheClient;
import gov.hhs.cdc.trustedintermediary.wrappers.ClientConnection;
import java.io.IOException;
import javax.inject.Inject;

public class ReportStreamConnection implements ClientConnection {

    private String token;
    private final String URI = "http://reportstream.endpoint";
    @Inject private ApacheClient client;

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
}
