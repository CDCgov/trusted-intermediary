package gov.hhs.cdc.trustedintermediary.wrappers;

import java.util.Map;

/** This interface provides a generic blueprint for HTTP operations */
public interface HttpClient {
    String post(String url, Map<String, String> headerMap, String body) throws HttpClientException;

    String get(String url, Map<String, String> headerMap) throws HttpClientException;
}
