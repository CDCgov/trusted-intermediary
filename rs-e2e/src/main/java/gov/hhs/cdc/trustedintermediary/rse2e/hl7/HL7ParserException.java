package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

/**
 * The HL7ParserException class is responsible for handling exceptions that occur in the HL7Parser
 * and HL7ExpressionEvaluator class.
 */
public class HL7ParserException extends RuntimeException {

    public HL7ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public HL7ParserException(String message) {
        super(message);
    }
}
