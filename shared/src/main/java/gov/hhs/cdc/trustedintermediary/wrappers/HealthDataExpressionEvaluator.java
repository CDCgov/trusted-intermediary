package gov.hhs.cdc.trustedintermediary.wrappers;

/** Represents an interface for evaluating expressions on health data objects. */
public interface HealthDataExpressionEvaluator {
    boolean evaluateExpression(String expression, HealthData<?>... data)
            throws IllegalArgumentException;
}
