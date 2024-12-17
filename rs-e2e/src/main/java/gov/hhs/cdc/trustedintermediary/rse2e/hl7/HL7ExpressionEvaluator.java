package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The HapiHL7ExpressionEvaluator class is responsible for evaluating expressions on HL7 messages.
 * The expressions can be used to compare fields, count segments, and check for membership. The
 * expressions are in the form of: `field = value`, `field != value`, `field in (value1, value2)`,
 * `field.count() = value`, etc. The field can be a literal value (e.g. 'EPIC') or a field reference
 * (e.g. MSH-5.1, input.MSH-5.1).
 */
public class HL7ExpressionEvaluator implements HealthDataExpressionEvaluator {

    private static final HL7ExpressionEvaluator INSTANCE = new HL7ExpressionEvaluator();

    private static final Pattern OPERATION_PATTERN =
            Pattern.compile("^(\\S+)\\s*(=|!=|in)\\s*(.+)$");
    private static final Pattern HL7_COUNT_PATTERN = Pattern.compile("(\\S+)\\.count\\(\\)");
    private static final Pattern LITERAL_VALUE_PATTERN = Pattern.compile("'(.*)'");
    private static final Pattern LITERAL_VALUE_COLLECTION_PATTERN =
            Pattern.compile("\\(([^)]+)\\)");
    private static final Pattern MESSAGE_SOURCE_PATTERN =
            Pattern.compile("(input|output)?\\.?(\\S+)");

    private HL7ExpressionEvaluator() {}

    public static HL7ExpressionEvaluator getInstance() {
        return INSTANCE;
    }

    @Override
    public final boolean evaluateExpression(String expression, HealthData<?>... data) {
        if (data.length > 2) {
            throw new IllegalArgumentException(
                    "Expected two messages, but received: " + data.length);
        }

        Matcher matcher = OPERATION_PATTERN.matcher(expression);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid statement format.");
        }

        String leftOperand = matcher.group(1); // e.g. MSH-5.1, input.MSH-5.1, 'EPIC', OBR.count()
        String operator = matcher.group(2); // `=`, `!=`, or `in`
        String rightOperand =
                matcher.group(3); // e.g. MSH-5.1, input.MSH-5.1, 'EPIC', ('EPIC', 'CERNER'), 2

        HL7Message outputMessage = (HL7Message) data[0];
        HL7Message inputMessage = (data.length > 1) ? (HL7Message) data[1] : null;

        // matches a count operation (e.g. OBR.count())
        Matcher hl7CountMatcher = HL7_COUNT_PATTERN.matcher(leftOperand);
        if (hl7CountMatcher.matches()) {
            return evaluateCollectionCount(
                    outputMessage, hl7CountMatcher.group(1), rightOperand, operator);
        }

        // matches either a literal value (e.g. 'EPIC') or a field reference (e.g. MSH-5.1,
        // input.MSH-5.1)
        String leftValue = getLiteralOrFieldValue(outputMessage, inputMessage, leftOperand);

        // matches membership operator (e.g. MSH-5.1 in ('EPIC', 'CERNER'))
        if (operator.equals("in")) {
            return evaluateMembership(leftValue, rightOperand);
        }

        // matches either a literal value (e.g. 'EPIC') or a field reference (e.g. MSH-5.1,
        // input.MSH-5.1)
        String rightValue = getLiteralOrFieldValue(outputMessage, inputMessage, rightOperand);

        // matches equality operators (e.g. MSH-5.1 = 'EPIC', MSH-5.1 != 'EPIC')
        return evaluateEquality(leftValue, rightValue, operator);
    }

    protected <T extends Comparable<T>> boolean evaluateEquality(
            T leftValue, T rightValue, String operator) {
        if (operator.equals("=")) {
            return leftValue.equals(rightValue);
        } else if (operator.equals("!=")) {
            return !leftValue.equals(rightValue);
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }

    protected boolean evaluateMembership(String leftValue, String rightOperand) {
        Matcher literalValueCollectionMatcher =
                LITERAL_VALUE_COLLECTION_PATTERN.matcher(rightOperand);
        if (!literalValueCollectionMatcher.matches()) {
            throw new IllegalArgumentException("Invalid collection format: " + rightOperand);
        }
        String arrayString = literalValueCollectionMatcher.group(1);
        Set<String> values =
                Arrays.stream(arrayString.split(","))
                        .map(s -> s.trim().replace("'", ""))
                        .collect(Collectors.toSet());
        return values.contains(leftValue);
    }

    protected boolean evaluateCollectionCount(
            HL7Message message, String segmentName, String rightOperand, String operator) {
        try {
            int count = message.getSegmentCount(segmentName);
            int rightValue = Integer.parseInt(rightOperand);
            return evaluateEquality(count, rightValue, operator);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Error evaluating collection count. Segment: "
                            + segmentName
                            + ", count: "
                            + rightOperand,
                    e);
        }
    }

    protected String getLiteralOrFieldValue(
            HL7Message outputMessage, HL7Message inputMessage, String operand) {
        Matcher literalValueMatcher = LITERAL_VALUE_PATTERN.matcher(operand);
        if (literalValueMatcher.matches()) {
            return literalValueMatcher.group(1);
        }
        return getFieldValue(outputMessage, inputMessage, operand);
    }

    protected String getFieldValue(
            HL7Message outputMessage, HL7Message inputMessage, String fieldName) {
        Matcher messageSourceMatcher = MESSAGE_SOURCE_PATTERN.matcher(fieldName);
        if (!messageSourceMatcher.matches()) {
            throw new IllegalArgumentException("Invalid field name format: " + fieldName);
        }

        String fileSource = messageSourceMatcher.group(1);
        HL7Message message = getMessageBySource(fileSource, inputMessage, outputMessage);

        try {
            return message.getValue(fieldName);
        } catch (HL7MessageException e) {
            return null;
        }
    }

    protected HL7Message getMessageBySource(
            String source, HL7Message inputMessage, HL7Message outputMessage) {
        if ("input".equals(source)) {
            if (inputMessage == null) {
                throw new IllegalArgumentException("Input message is null for: " + source);
            }
            return inputMessage;
        }
        return outputMessage;
    }
}
