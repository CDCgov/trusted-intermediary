package gov.hhs.cdc.trustedintermediary.domainconnector;

import java.util.HashMap;
import java.util.Map;

public class DomainResponse {
    private int statusCode;
    private String body = "";
    private Map<String, String> headers = new HashMap<>();

    public DomainResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

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
