package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

public class RuleExecutionException extends Exception {
    public RuleExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleExecutionException(String message) {
        super(message);
    }
}
