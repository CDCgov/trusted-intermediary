package gov.hhs.cdc.trustedintermediary.utils;

public class RetryFailedException extends Exception {
    public RetryFailedException(String message) {
        super(message);
    }

    public RetryFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
