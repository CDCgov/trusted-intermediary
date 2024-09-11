package gov.hhs.cdc.trustedintermediary.rse2e;

import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.HL7Message;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HL7ExpressionEvaluator {

    public static boolean parseAndEvaluate(
            HL7Message inputMessage, HL7Message outputMessage, String statement) {

        Pattern pattern =
                Pattern.compile(
                        "(input|output)?\\.?(\\w+(-\\d+\\.\\d+)?)\\s*(=|!=|in)\\s*(?:'([^']*)'|\\(([^)]*)\\)|([A-Z]+-\\d+\\.\\d+))");
        Matcher matcher = pattern.matcher(statement);

        if (matcher.matches()) {
            String leftFile =
                    matcher.group(1) != null ? matcher.group(1) : "output"; // Default to "output"
            String leftField = matcher.group(2); // The HL7 field (e.g., ORC-4.1)
            String operator = matcher.group(4); // `=`, `!=`, or `in`
            String literalValue = matcher.group(5); // The literal value for `=` or `!=`, or null
            String inValues = matcher.group(6); // The list of values for the `in` operator, or null
            String rightField = matcher.group(7); // The right-hand side field, or null

            HL7Message leftMessage = "input".equals(leftFile) ? inputMessage : outputMessage;
            String leftValue = leftMessage.getField(leftField);

            if ("in".equals(operator) && inValues != null) {
                Set<String> valuesSet = new HashSet<>(Arrays.asList(inValues.split("\\s*,\\s*")));
                return valuesSet.contains(leftValue);
            }

            // Handle `=` or `!=` with literal value
            if (literalValue != null) {
                return evaluateComparison(leftValue, literalValue, operator);
            }

            throw new IllegalArgumentException("Invalid statement format.");
        } else {
            throw new IllegalArgumentException("Invalid statement format.");
        }
    }

    private static boolean evaluateComparison(
            String leftValue, String rightValue, String operator) {
        if ("=".equals(operator)) {
            return leftValue != null && leftValue.equals(rightValue);
        } else if ("!=".equals(operator)) {
            return leftValue != null && !leftValue.equals(rightValue);
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }
}
