package gov.hhs.cdc.trustedintermediary.domainconnector;

import java.util.Map;

public class DomainRequest {
    private String body;
    private String url;
    private Map<String, String> headers;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
