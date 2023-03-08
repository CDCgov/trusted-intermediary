package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;
import java.util.Map;

public interface HttpClient {
    String post(String path, Map<String, String> headerMap, String body) throws IOException;

    String get(String url, Map<String, String> headerMap, String body) throws IOException;
}
