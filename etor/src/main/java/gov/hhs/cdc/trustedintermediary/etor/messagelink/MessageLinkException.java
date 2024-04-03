package gov.hhs.cdc.trustedintermediary.etor.messagelink;

public class MessageLinkException extends Exception {

    public MessageLinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageLinkException(String message) {
        super(message);
    }
}
