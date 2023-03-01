package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;

public interface HttpClient {
    String post(String path, String body, String bearertoken) throws IOException;

    String requestToken(String s, String body, String token) throws IOException;
}
