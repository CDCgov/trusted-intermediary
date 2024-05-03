package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

/** Custom exception class use to catch RuleLoader exceptions */
public class RuleLoaderException extends Exception {
    public RuleLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
