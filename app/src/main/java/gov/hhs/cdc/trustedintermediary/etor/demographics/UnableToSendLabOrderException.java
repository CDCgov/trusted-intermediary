package gov.hhs.cdc.trustedintermediary.etor.demographics;

/**
 * This exception class gets triggered when any exception occurs during the process of sending a lab
 * order
 */
public class UnableToSendLabOrderException extends Exception {
    public UnableToSendLabOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
