package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

/**
 * The HL7MessageException class is responsible for handling exceptions that occur in the HL7Message
 * class.
 */
public class HL7MessageException extends Exception {

    public HL7MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public HL7MessageException(String message) {
        super(message);
    }
}
