package gov.hhs.cdc.trustedintermediary.rse2e;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.HL7Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HL7ExpressionEvaluator {

    public static boolean parseAndEvaluate(
            HL7Message<Message> inputMessage, HL7Message<Message> outputMessage, String statement) {

        Pattern operationPattern = Pattern.compile("^(\\S+)\\s*(=|!=|in)\\s*(.+)$");
        Matcher matcher = operationPattern.matcher(statement);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid statement format.");
        }

        String leftOperand = matcher.group(1);
        String operator = matcher.group(2); // `=`, `!=`, or `in`
        String rightOperand = matcher.group(3);

        // TODO - check if left operand is a count operation
        Pattern hl7CountPattern = Pattern.compile("(\\S+)\\.count\\(\\)");

        Pattern literalValuePattern = Pattern.compile("'(\\S+)'");
        Matcher leftLiteralValueMatcher = literalValuePattern.matcher(leftOperand);
        String leftValue =
                leftLiteralValueMatcher.matches()
                        ? leftLiteralValueMatcher.group(1)
                        : getFieldValue(inputMessage.getMessage(), leftOperand);

        if (operator.equals("in")) {
            return evaluateMembership(leftValue, rightOperand);
        }

        Matcher rightLiteralValueMatcher = literalValuePattern.matcher(rightOperand);
        String rightValue =
                rightLiteralValueMatcher.matches()
                        ? rightLiteralValueMatcher.group(1)
                        : getFieldValue(inputMessage.getMessage(), rightOperand);

        if (operator.equals("=")) {
            return leftValue.equals(rightValue);
        } else if (operator.equals("!=")) {
            return !leftValue.equals(rightValue);
        }

        throw new IllegalArgumentException("Unknown operator: " + operator);
    }

    private static boolean evaluateMembership(String leftValue, String rightOperand) {
        Pattern literalValueCollectionPattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher literalValueCollectionMatcher = literalValueCollectionPattern.matcher(rightOperand);
        if (!literalValueCollectionMatcher.matches()) {
            return false;
        }
        String arrayString = literalValueCollectionMatcher.group(1);
        ArrayList<String> values =
                Arrays.stream(arrayString.split(","))
                        .map(s -> s.trim().replaceAll("^'|'$", ""))
                        .collect(Collectors.toCollection(ArrayList::new));
        return values.contains(leftValue);
    }

    private static String getFieldValue(Message message, String fieldName) {
        Pattern hl7FieldNamePattern = Pattern.compile("(input|output)?\\.?(\\S+)-(\\S+)");
        Matcher hl7FieldNameMatcher = hl7FieldNamePattern.matcher(fieldName);
        if (!hl7FieldNameMatcher.matches()) {
            return "";
        }

        // TODO - handle input/output
        String file = hl7FieldNameMatcher.group(1);
        String segmentName = hl7FieldNameMatcher.group(2);
        String index = hl7FieldNameMatcher.group(3);
        String[] fieldComponents = index.split("\\.");
        try {
            Segment segment = (Segment) message.get(segmentName);
            if (fieldComponents.length == 0) { // e.g. MSH
                return segment.encode();
            } else if (fieldComponents.length == 1) { // e.g. MSH-9
                return segment.getField(Integer.parseInt(fieldComponents[0]), 0).encode();
            } else if (fieldComponents.length == 2) { // e.g. MSH-9.2
                String field = segment.getField(Integer.parseInt(fieldComponents[0]), 0).encode();
                // TODO - get the separator from the message instead of assuming
                String[] subfieldsArray = field.split("\\^");
                if (subfieldsArray.length > 1) {
                    // TODO - validate that we're not getting a sub-zero result after subtracting 1?
                    return subfieldsArray[Integer.parseInt(fieldComponents[1]) - 1];
                }
                return "";
            } else {
                return "";
            }
        } catch (Exception e) {
            // log exception
            return "";
        }
    }
}
