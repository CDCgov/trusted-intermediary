package gov.hhs.cdc.trustedintermediary.etor.messages;

/** This exception is thrown when there is an error sending a message. */
public class UnableToSendMessageException extends Exception {
    public UnableToSendMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
