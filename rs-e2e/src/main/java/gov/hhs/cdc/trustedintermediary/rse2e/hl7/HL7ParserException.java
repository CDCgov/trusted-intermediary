package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

public class HL7ParserException extends RuntimeException {

    public HL7ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public HL7ParserException(String message) {
        super(message);
    }
}
