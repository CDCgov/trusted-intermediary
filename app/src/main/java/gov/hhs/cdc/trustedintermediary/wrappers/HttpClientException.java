package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;

/** Custom exception class use to catch any exception coming from an HTTP request */
public class HttpClientException extends IOException {

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
