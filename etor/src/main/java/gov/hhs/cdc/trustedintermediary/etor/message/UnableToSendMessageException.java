package gov.hhs.cdc.trustedintermediary.etor.message;

public class UnableToSendMessageException extends Exception {
    public UnableToSendMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
