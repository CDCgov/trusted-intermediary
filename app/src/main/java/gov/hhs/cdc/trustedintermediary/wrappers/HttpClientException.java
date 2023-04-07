package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;

public class HttpClientException extends IOException {

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
