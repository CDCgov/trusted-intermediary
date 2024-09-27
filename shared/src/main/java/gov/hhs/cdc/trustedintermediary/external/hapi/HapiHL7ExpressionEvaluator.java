package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator;
import java.util.ArrayList;
import java.util.Arrays;
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
public class HapiHL7ExpressionEvaluator implements HealthDataExpressionEvaluator {

    private static final HapiHL7ExpressionEvaluator INSTANCE = new HapiHL7ExpressionEvaluator();

    private static final String NEWLINE_REGEX = "\\r?\\n|\\r";
    private static final Pattern OPERATION_PATTERN =
            Pattern.compile("^(\\S+)\\s*(=|!=|in)\\s*(.+)$");
    private static final Pattern HL7_COUNT_PATTERN = Pattern.compile("(\\S+)\\.count\\(\\)");
    private static final Pattern LITERAL_VALUE_PATTERN = Pattern.compile("'(.*)'");

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

        Matcher matcher = OPERATION_PATTERN.matcher(expression);

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
            return leftValue.compareTo(rightValue) == 0;
        } else if (operator.equals("!=")) {
            return leftValue.compareTo(rightValue) != 0;
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }

    protected boolean evaluateMembership(String leftValue, String rightOperand) {
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

    protected boolean evaluateCollectionCount(
            Message message, String segmentName, String rightOperand, String operator) {
        try {
            int count = countSegments(message.encode(), segmentName);
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

    protected String getLiteralOrFieldValue(
            Message outputMessage, Message inputMessage, String operand) {
        Matcher literalValueMatcher = LITERAL_VALUE_PATTERN.matcher(operand);
        if (literalValueMatcher.matches()) {
            return literalValueMatcher.group(1);
        }
        return getFieldValue(outputMessage, inputMessage, operand);
    }

    protected String getFieldValue(Message outputMessage, Message inputMessage, String fieldName) {
        Pattern messageSourcePattern = Pattern.compile("(input|output)?\\.?(\\S+)");
        Matcher messageSourceMatcher = messageSourcePattern.matcher(fieldName);
        if (!messageSourceMatcher.matches()) {
            throw new IllegalArgumentException("Invalid field name format: " + fieldName);
        }

        String fileSource = messageSourceMatcher.group(1);
        String fieldNameWithoutFileSource = messageSourceMatcher.group(2);
        Message message = getMessageBySource(fileSource, inputMessage, outputMessage);

        try {
            String messageString = message.encode();
            char fieldSeparator = message.getFieldSeparatorValue();
            String encodingCharacters = message.getEncodingCharactersValue();
            return getSegmentFieldValue(
                    messageString, fieldNameWithoutFileSource, fieldSeparator, encodingCharacters);
        } catch (HL7Exception | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Failed to extract field value for: " + fieldName, e);
        }
    }

    // We decided to implement our own simple HL7 parser as the Hapi library was not adequate for
    // our needs.
    protected static String getSegmentFieldValue(
            String hl7Message, String fieldName, char fieldSeparator, String encodingCharacters) {

        Pattern hl7FieldNamePattern = Pattern.compile("(\\w+)(?:-(\\S+))?");
        Matcher hl7FieldNameMatcher = hl7FieldNamePattern.matcher(fieldName);
        if (!hl7FieldNameMatcher.matches()) {
            throw new IllegalArgumentException("Invalid HL7 field format: " + fieldName);
        }

        String segmentName = hl7FieldNameMatcher.group(1);
        String segmentFieldIndex = hl7FieldNameMatcher.group(2);

        String[] lines = hl7Message.split(NEWLINE_REGEX);
        for (String line : lines) {
            if (!line.startsWith(segmentName)) {
                continue;
            }

            if (segmentFieldIndex == null) {
                return line;
            }

            String[] fields = line.split(Pattern.quote(String.valueOf(fieldSeparator)));
            String[] indexParts = segmentFieldIndex.split("\\.");

            try {
                int fieldPos = Integer.parseInt(indexParts[0]);

                if (segmentName.equals("MSH")) {
                    fieldPos--;
                }

                if (fieldPos < 0 || fieldPos >= fields.length) {
                    throw new IllegalArgumentException(
                            "Invalid field index (out of bounds): " + segmentFieldIndex);
                }

                String field = fields[fieldPos];

                if (indexParts.length == 1 || field.isEmpty()) {
                    return field;
                }

                int subFieldEncodingCharactersIndex = indexParts.length - 2;
                if (subFieldEncodingCharactersIndex >= encodingCharacters.length()) {
                    throw new IllegalArgumentException(
                            "Invalid subfield index (out of bounds): " + segmentFieldIndex);
                }
                char subfieldSeparator = encodingCharacters.charAt(subFieldEncodingCharactersIndex);
                String[] subfields = field.split(Pattern.quote(String.valueOf(subfieldSeparator)));
                int subFieldPos = Integer.parseInt(indexParts[1]) - 1;
                return subFieldPos >= 0 && subFieldPos < subfields.length
                        ? subfields[subFieldPos]
                        : "";
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid field index formatting: " + segmentFieldIndex, e);
            }
        }

        return null;
    }

    protected static int countSegments(String hl7Message, String segmentName) {
        return (int)
                Arrays.stream(hl7Message.split(NEWLINE_REGEX))
                        .filter(line -> line.startsWith(segmentName))
                        .count();
    }

    protected Message getMessageBySource(
            String source, Message inputMessage, Message outputMessage) {
        if ("input".equals(source)) {
            if (inputMessage == null) {
                throw new IllegalArgumentException("Input message is null for: " + source);
            }
            return inputMessage;
        }
        return outputMessage;
    }
}
