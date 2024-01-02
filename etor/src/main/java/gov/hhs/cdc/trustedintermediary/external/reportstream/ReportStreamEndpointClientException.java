package gov.hhs.cdc.trustedintermediary.external.reportstream;

/**
 * This exception class gets triggered when any exception occurs when sending a request to a RS API
 */
public class ReportStreamEndpointClientException extends Exception {
    public ReportStreamEndpointClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
