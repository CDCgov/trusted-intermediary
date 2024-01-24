package gov.hhs.cdc.trustedintermediary.etor.messages;

public class UnableToSendMessageException extends Exception {
    public UnableToSendMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
