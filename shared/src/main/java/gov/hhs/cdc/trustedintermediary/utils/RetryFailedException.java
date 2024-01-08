package gov.hhs.cdc.trustedintermediary.utils;

/**
 * Custom exception that wraps the last exception thrown after the maximum number of retries has
 * been reached. It could also be thrown in the case of a InterruptedException.
 */
public class RetryFailedException extends Exception {

    public RetryFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
