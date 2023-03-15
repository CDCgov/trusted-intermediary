package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;
import java.util.Map;

/** This interface provides a generic blueprint for HTTP operations */
public interface HttpClient {
    String post(String path, Map<String, String> headerMap, String body) throws IOException;
}
