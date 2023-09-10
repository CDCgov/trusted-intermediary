package gov.hhs.cdc.trustedintermediary.wrappers;

/** Custom exception class use to catch any exception coming from an HTTP request */
public class HttpClientException extends Exception {

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
