package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

/**
 * The HL7FileMatcherException class is responsible for handling exceptions that occur in the
 * HL7FileMatcher class.
 */
public class HL7FileMatcherException extends Exception {

    public HL7FileMatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public HL7FileMatcherException(String message) {
        super(message);
    }
}
