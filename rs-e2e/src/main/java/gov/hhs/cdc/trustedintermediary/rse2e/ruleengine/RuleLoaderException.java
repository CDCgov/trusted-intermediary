package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

/** Custom exception class use to catch RuleLoader exceptions */
public class RuleLoaderException extends Exception {
    public RuleLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
