package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

public class RuleLoaderException extends Exception {

    public RuleLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleLoaderException(String message) {
        super(message);
    }
}
