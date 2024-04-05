package gov.hhs.cdc.trustedintermediary.etor.messagelink;

/** This exception is thrown when there is an error linking messages. */
public class MessageLinkException extends Exception {

    public MessageLinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageLinkException(String message) {
        super(message);
    }
}
