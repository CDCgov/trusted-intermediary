package gov.hhs.cdc.trustedintermediary.rse2e;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.HL7Message;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HL7ExpressionEvaluator {

    private static final Pattern operationPattern =
            Pattern.compile("^(\\S+)\\s*(=|!=|in)\\s*(.+)$");
    private static final Pattern hl7FieldNamePattern =
            Pattern.compile("(input|output)?\\.?(\\S+)-(\\S+)");
    private static final Pattern literalValuePattern = Pattern.compile("'(\\S+)'");
    private static final Pattern literalValueCollectionPattern = Pattern.compile("\\(([^)]+)\\)");
    private static final Pattern hl7CountPattern = Pattern.compile("(\\S+)\\.count\\(\\)");

    public static boolean parseAndEvaluate(
            HL7Message<Message> inputMessage, HL7Message<Message> outputMessage, String statement) {

        Matcher matcher = operationPattern.matcher(statement);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid statement format.");
        }

        String leftOperand = matcher.group(1);
        String operator = matcher.group(2); // `=`, `!=`, or `in`
        String rightOperand = matcher.group(3); // The right-hand side field, or null

        Matcher leftLiteralValueMatcher = literalValuePattern.matcher(leftOperand);
        Matcher rightLiteralValueMatcher = literalValuePattern.matcher(rightOperand);

        String leftValue =
                leftLiteralValueMatcher.matches()
                        ? leftLiteralValueMatcher.group(1)
                        : String.valueOf(getFieldValue(inputMessage.getMessage(), leftOperand));
        String rightValue =
                rightLiteralValueMatcher.matches()
                        ? rightLiteralValueMatcher.group(1)
                        : String.valueOf(getFieldValue(inputMessage.getMessage(), rightOperand));

        switch (operator) {
            case "=" -> {
                return leftValue.equals(rightValue);
            }
            case "!=" -> {
                return !leftValue.equals(rightValue);
            }
            case "in" -> {
                Matcher literalValueCollectionMatcher =
                        literalValueCollectionPattern.matcher(rightOperand);
                if (literalValueCollectionMatcher.matches()) {
                    String inValues = literalValueCollectionMatcher.group(1);
                    return false;
                }
            }
        }

        return false;
    }

    private static Optional<String> getFieldValue(Message message, String fieldName) {
        Matcher hl7FieldNameMatcher = hl7FieldNamePattern.matcher(fieldName);

        if (!hl7FieldNameMatcher.matches()) {
            return Optional.empty();
        }

        // TODO - handle input/output
        String file = hl7FieldNameMatcher.group(1);
        String segmentName = hl7FieldNameMatcher.group(2);
        String index = hl7FieldNameMatcher.group(3);
        String[] fieldComponents = index.split("\\.");
        try {
            Segment segment = (Segment) message.get(segmentName);
            if (fieldComponents.length == 0) { // e.g. MSH
                return Optional.of(segment.encode());
            } else if (fieldComponents.length == 1) { // e.g. MSH-9
                return Optional.of(
                        segment.getField(Integer.parseInt(fieldComponents[0]), 0).encode());
            } else if (fieldComponents.length == 2) { // e.g. MSH-9.2
                String field = segment.getField(Integer.parseInt(fieldComponents[0]), 0).encode();
                // TODO - get the separator from the message instead of assuming
                String[] subfieldsArray = field.split("\\^");
                if (subfieldsArray.length > 1) {
                    // TODO - validate that we're not getting a sub-zero result after subtracting 1?
                    return Optional.of(subfieldsArray[Integer.parseInt(fieldComponents[1]) - 1]);
                }
                return Optional.empty();
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            // log exception
            return Optional.empty();
        }
    }

    //    private static boolean evaluateComparison(
    //            String leftValue, String rightValue, String operator) {
    //        if ("=".equals(operator)) {
    //            return leftValue != null && leftValue.equals(rightValue);
    //        } else if ("!=".equals(operator)) {
    //            return leftValue != null && !leftValue.equals(rightValue);
    //        }
    //        throw new IllegalArgumentException("Unknown operator: " + operator);
    //    }
}
