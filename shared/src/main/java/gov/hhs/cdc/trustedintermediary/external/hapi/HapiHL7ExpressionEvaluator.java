package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class HapiHL7ExpressionEvaluator implements HealthDataExpressionEvaluator {

    private static final HapiHL7ExpressionEvaluator INSTANCE = new HapiHL7ExpressionEvaluator();

    @Inject Logger logger;

    private HapiHL7ExpressionEvaluator() {}

    public static HapiHL7ExpressionEvaluator getInstance() {
        return INSTANCE;
    }

    @Override
    public final boolean evaluateExpression(String expression, HealthData<?>... data) {
        if (data.length > 2) {
            throw new IllegalArgumentException(
                    "Expected two messages, but received: " + data.length);
        }

        Pattern operationPattern = Pattern.compile("^(\\S+)\\s*(=|!=|in)\\s*(.+)$");
        Matcher matcher = operationPattern.matcher(expression);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid statement format.");
        }

        String leftOperand = matcher.group(1); // e.g. MSH-5.1, input.MSH-5.1, 'EPIC', OBR.count()
        String operator = matcher.group(2); // `=`, `!=`, or `in`
        String rightOperand =
                matcher.group(3); // e.g. MSH-5.1, input.MSH-5.1, 'EPIC', ('EPIC', 'CERNER'), 2

        Message outputMessage = (Message) data[0].getUnderlyingData();
        Message inputMessage = (data.length > 1) ? (Message) data[1].getUnderlyingData() : null;

        // matches a count operation (e.g. OBR.count())
        Pattern hl7CountPattern = Pattern.compile("(\\S+)\\.count\\(\\)");
        Matcher hl7CountMatcher = hl7CountPattern.matcher(leftOperand);
        if (hl7CountMatcher.matches()) {
            return evaluateCollectionCount(
                    outputMessage, hl7CountMatcher.group(1), rightOperand, operator);
        }

        // matches either a literal value (e.g. 'EPIC') or a field reference (e.g. MSH-5.1,
        // input.MSH-5.1)
        Pattern literalValuePattern = Pattern.compile("'(\\S+)'");
        Matcher leftLiteralValueMatcher = literalValuePattern.matcher(leftOperand);
        String leftValue =
                leftLiteralValueMatcher.matches()
                        ? leftLiteralValueMatcher.group(1)
                        : getFieldValue(outputMessage, inputMessage, leftOperand);

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
                        : getFieldValue(outputMessage, inputMessage, rightOperand);

        // matches equality operators (e.g. MSH-5.1 = 'EPIC', MSH-5.1 != 'EPIC')
        return evaluateEquality(leftValue, rightValue, operator);
    }

    private <T extends Comparable<T>> boolean evaluateEquality(
            T leftValue, T rightValue, String operator) {
        if (operator.equals("=")) {
            return leftValue.compareTo(rightValue) == 0;
        } else if (operator.equals("!=")) {
            return leftValue.compareTo(rightValue) != 0;
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }

    private boolean evaluateMembership(String leftValue, String rightOperand) {
        Pattern literalValueCollectionPattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher literalValueCollectionMatcher = literalValueCollectionPattern.matcher(rightOperand);
        if (!literalValueCollectionMatcher.matches()) {
            throw new IllegalArgumentException("Invalid collection format: " + rightOperand);
        }
        String arrayString = literalValueCollectionMatcher.group(1);
        ArrayList<String> values =
                Arrays.stream(arrayString.split(","))
                        .map(s -> s.trim().replace("'", ""))
                        .collect(Collectors.toCollection(ArrayList::new));
        return values.contains(leftValue);
    }

    private boolean evaluateCollectionCount(
            Message message, String segmentName, String rightOperand, String operator) {
        try {
            int count = message.getAll(segmentName).length;
            int rightValue = Integer.parseInt(rightOperand);
            return evaluateEquality(count, rightValue, operator);
        } catch (HL7Exception | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Error evaluating collection count. Segment: "
                            + segmentName
                            + ", count: "
                            + rightOperand,
                    e);
        }
    }

    private String getFieldValue(Message outputMessage, Message inputMessage, String fieldName) {
        Pattern hl7FieldNamePattern = Pattern.compile("(input|output)?\\.?(\\S+)-(\\S+)");
        Matcher hl7FieldNameMatcher = hl7FieldNamePattern.matcher(fieldName);
        if (!hl7FieldNameMatcher.matches()) {
            throw new IllegalArgumentException("Invalid field name format: " + fieldName);
        }

        Message message =
                getMessageBySource(hl7FieldNameMatcher.group(1), inputMessage, outputMessage);

        String segmentName = hl7FieldNameMatcher.group(2);
        String index = hl7FieldNameMatcher.group(3);
        String[] fieldComponents = index.split("\\.");

        try {
            Segment segment = (Segment) message.get(segmentName);
            return extractField(segment, fieldComponents);
        } catch (HL7Exception | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Failed to extract field value for: " + fieldName, e);
        }
    }

    private String extractField(Segment segment, String[] fieldComponents)
            throws HL7Exception, NumberFormatException {

        // fieldComponents looks like 'MSH'
        if (fieldComponents.length == 0) { // e.g. MSH
            return segment.encode();
        }

        // When the `|` character is encoded, it shows as `\F\`, the field separator
        String field = segment.getField(Integer.parseInt(fieldComponents[0]), 0).encode();

        // fieldComponents looks like 'MSH-9'
        if (fieldComponents.length == 1) {
            return field;
        }

        // fieldComponents looks like 'MSH-9.2'
        // Since we control the sample files, we can assume the field separator is the standard `^`
        String[] subfieldsArray = field.split("\\^");
        int subFieldIndex = Integer.parseInt(fieldComponents[1]) - 1;

        return subFieldIndex >= 0 && subFieldIndex < subfieldsArray.length
                ? subfieldsArray[subFieldIndex]
                : "";
    }

    private Message getMessageBySource(String source, Message inputMessage, Message outputMessage) {
        return "input".equals(source) ? inputMessage : outputMessage;
    }
}
