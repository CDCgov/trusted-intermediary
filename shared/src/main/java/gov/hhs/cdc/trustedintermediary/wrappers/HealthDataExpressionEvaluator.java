package gov.hhs.cdc.trustedintermediary.wrappers;

public interface HealthDataExpressionEvaluator {
    boolean evaluateExpression(String expression, HealthData<?>... data)
            throws IllegalArgumentException;
}
