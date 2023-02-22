package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.wrappers.ClientConnection;

public class ReportStreamConnection implements ClientConnection {

    private String token;

    private ReportStreamConnection() {}

    private static final ReportStreamConnection INSTANCE = new ReportStreamConnection();

    public static ReportStreamConnection getInstance() {
        return INSTANCE;
    }

    @Override
    public void sendRequestBody(String json) {
        // TODO logic
    }

    public ReportStreamConnection setToken(String token) {
        this.token = token;
        return this;
    }
}
