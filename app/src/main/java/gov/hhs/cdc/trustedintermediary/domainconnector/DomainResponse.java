package gov.hhs.cdc.trustedintermediary.domainconnector;

import java.util.Map;

public class DomainResponse {
    private String body;
    private Map<String, String> headers;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
