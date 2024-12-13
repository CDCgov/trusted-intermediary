package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

/**
 * The HapiHL7FileMatcherException class is responsible for handling exceptions that occur in the
 * HapiHL7FileMatcher class.
 */
public class HapiHL7FileMatcherException extends Exception {

    public HapiHL7FileMatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public HapiHL7FileMatcherException(String message) {
        super(message);
    }
}
