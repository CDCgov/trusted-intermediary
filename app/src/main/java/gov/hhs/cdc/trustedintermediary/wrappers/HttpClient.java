package gov.hhs.cdc.trustedintermediary.wrappers;
/** This interface provides a generic blueprint for CRUD operations */
import java.io.IOException;
import java.util.Map;

public interface HttpClient {
    String post(String path, Map<String, String> headerMap, String body) throws IOException;
}
