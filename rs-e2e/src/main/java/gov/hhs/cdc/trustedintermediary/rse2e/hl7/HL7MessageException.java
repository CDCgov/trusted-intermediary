package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

public class HL7MessageException extends Exception {

    public HL7MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public HL7MessageException(String message) {
        super(message);
    }
}
