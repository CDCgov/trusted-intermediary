package gov.hhs.cdc.trustedintermediary.etor.orders;

/**
 * This exception class gets triggered when any exception occurs during the process of sending a lab
 * order
 */
public class UnableToSendOrderException extends Exception {
    public UnableToSendOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
