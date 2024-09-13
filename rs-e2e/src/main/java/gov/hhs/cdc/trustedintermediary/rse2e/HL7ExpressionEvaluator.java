package gov.hhs.cdc.trustedintermediary.rse2e;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HL7ExpressionEvaluator {

    public static boolean parseAndEvaluate(
            Message outputMessage, Message inputMessage, String statement) {

        Pattern operationPattern = Pattern.compile("^(\\S+)\\s*(=|!=|in)\\s*(.+)$");
        Matcher matcher = operationPattern.matcher(statement);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid statement format.");
        }

        String leftOperand = matcher.group(1); // e.g. MSH-5.1, input.MSH-5.1, 'EPIC', OBR.count()
        String operator = matcher.group(2); // `=`, `!=`, or `in`
        String rightOperand =
                matcher.group(3); // e.g. MSH-5.1, input.MSH-5.1, 'EPIC', ('EPIC', 'CERNER'), 2

        // matches a count operation (e.g. OBR.count())
        Pattern hl7CountPattern = Pattern.compile("(\\S+)\\.count\\(\\)");
        Matcher hl7CountMatcher = hl7CountPattern.matcher(leftOperand);
        if (hl7CountMatcher.matches()) {
            return evaluateCollectionCount(
                    outputMessage.getMessage(), hl7CountMatcher.group(1), rightOperand, operator);
        }

        // matches either a literal value (e.g. 'EPIC') or a field reference (e.g. MSH-5.1,
        // input.MSH-5.1)
        Pattern literalValuePattern = Pattern.compile("'(\\S+)'");
        Matcher leftLiteralValueMatcher = literalValuePattern.matcher(leftOperand);
        String leftValue =
                leftLiteralValueMatcher.matches()
                        ? leftLiteralValueMatcher.group(1)
                        : getFieldValue(
                                outputMessage.getMessage(), inputMessage.getMessage(), leftOperand);

        // matches membership operator (e.g. MSH-5.1 in ('EPIC', 'CERNER'))
        if (operator.equals("in")) {
            return evaluateMembership(leftValue, rightOperand);
        }

        // matches either a literal value (e.g. 'EPIC') or a field reference (e.g. MSH-5.1,
        // input.MSH-5.1)
        Matcher rightLiteralValueMatcher = literalValuePattern.matcher(rightOperand);
        String rightValue =
                rightLiteralValueMatcher.matches()
                        ? rightLiteralValueMatcher.group(1)
                        : getFieldValue(
                                outputMessage.getMessage(),
                                inputMessage.getMessage(),
                                rightOperand);

        // matches equality operators (e.g. MSH-5.1 = 'EPIC', MSH-5.1 != 'EPIC')
        return evaluateEquality(leftValue, rightValue, operator);
    }

    private static <T extends Comparable<T>> boolean evaluateEquality(
            T leftValue, T rightValue, String operator) {
        if (operator.equals("=")) {
            return leftValue.compareTo(rightValue) == 0;
        } else if (operator.equals("!=")) {
            return leftValue.compareTo(rightValue) != 0;
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

    private static boolean evaluateCollectionCount(
            Message message, String segmentName, String rightOperand, String operator) {
        int count;
        try {
            count = message.getAll(segmentName).length;
        } catch (HL7Exception e) {
            return false;
        }
        int rightValue = Integer.parseInt(rightOperand);
        return evaluateEquality(count, rightValue, operator);
    }

    private static String getFieldValue(
            Message outputMessage, Message inputMessage, String fieldName) {
        Pattern hl7FieldNamePattern = Pattern.compile("(input|output)?\\.?(\\S+)-(\\S+)");
        Matcher hl7FieldNameMatcher = hl7FieldNamePattern.matcher(fieldName);
        if (!hl7FieldNameMatcher.matches()) {
            return "";
        }

        Message message =
                "input".equals(hl7FieldNameMatcher.group(1)) ? inputMessage : outputMessage;
        String segmentName = hl7FieldNameMatcher.group(2);
        String index = hl7FieldNameMatcher.group(3);
        String[] fieldComponents = index.split("\\.");
        try {
            Segment segment = (Segment) message.get(segmentName);
            if (fieldComponents.length == 0) { // e.g. MSH
                return segment.encode();
            } else if (fieldComponents.length == 1) { // e.g. MSH-9
                // Encoding the `|` character shows as `\F\`, the field separator
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
