package gov.hhs.cdc.trustedintermediary.domainconnector;

import java.util.HashMap;
import java.util.Map;

/** Represents a request coming into the domain that it should handle. */
public class DomainRequest {
    private String body = "";
    private String url = "";
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> pathParams = new HashMap<>();

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

    /**
     * Returns the headers for the request. Please note that the keys (header names) are always
     * lowercase.
     *
     * @return the headers Map for this request, with lowercase keys
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(Map<String, String> headers) {
        this.pathParams = headers;
    }
}
