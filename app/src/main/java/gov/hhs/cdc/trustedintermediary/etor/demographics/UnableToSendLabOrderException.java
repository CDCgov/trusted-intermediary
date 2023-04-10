package gov.hhs.cdc.trustedintermediary.etor.demographics;

public class UnableToSendLabOrderException extends Exception {
    public UnableToSendLabOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
