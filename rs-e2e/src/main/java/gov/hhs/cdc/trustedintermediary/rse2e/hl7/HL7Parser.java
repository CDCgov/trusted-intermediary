package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The HL7Parser class is responsible for parsing HL7 messages and extracting values from them. */
public class HL7Parser {
    static final String MSH_SEGMENT_NAME = "MSH";
    static final String NEWLINE_REGEX = "\\r?\\n|\\r";
    static final Pattern HL7_FIELD_NAME_PATTERN = Pattern.compile("(\\w+)-(\\d+(?:\\.\\d+)*)");

    private HL7Parser() {}

    public static HL7Message parseMessage(String content) {
        List<HL7Segment> segments = new ArrayList<>();
        String encodingCharactersField = null;
        String[] lines = content.split(NEWLINE_REGEX);
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] fields =
                    line.split(
                            Pattern.quote(String.valueOf(HL7Encoding.DEFAULT_FIELD_DELIMITER)), -1);
            String segmentName = fields[0];
            List<String> segmentFields =
                    new ArrayList<>(Arrays.asList(fields).subList(1, fields.length));
            if (Objects.equals(segmentName, MSH_SEGMENT_NAME)) {
                encodingCharactersField = fields[1];
                segmentFields.add(0, String.valueOf(HL7Encoding.DEFAULT_FIELD_DELIMITER));
            }
            segments.add(new HL7Segment(segmentName, segmentFields));
        }

        return new HL7Message(segments, HL7Encoding.fromEncodingField(encodingCharactersField));
    }

    public static String parseMessageFieldValue(HL7Path hl7Path, HL7Message message)
            throws HL7MessageException {
        if (hl7Path == null || hl7Path.indices().length == 0) {
            return "";
        }

        int[] indices = hl7Path.indices();
        List<String> fields = message.getSegment(hl7Path.segmentName()).fields();
        char[] delimiters = message.getEncoding().getOrderedDelimiters();

        if (fields == null || fields.isEmpty() || indices[0] > fields.size()) {
            return "";
        }

        String value = fields.get(indices[0] - 1);
        for (int i = 1; i < indices.length; i++) {
            if (i >= delimiters.length) {
                throw new HL7ParserException("Invalid HL7 path: too many sub-levels provided");
            }
            char segmentDelimiter = delimiters[i];
            int index = indices[i] - 1;
            String[] parts = value.split(Pattern.quote(String.valueOf(segmentDelimiter)));
            if (index < 0 || index >= parts.length) {
                return "";
            }
            value = parts[index];
        }
        return value;
    }

    public static HL7Path parsePath(String path) {
        Matcher matcher = HL7_FIELD_NAME_PATTERN.matcher(path);
        if (!matcher.matches()) {
            throw new HL7ParserException("Invalid HL7 path format: " + path);
        }

        String segmentName = matcher.group(1);
        int[] indices =
                Arrays.stream(matcher.group(2).split("\\.")).mapToInt(Integer::parseInt).toArray();

        return new HL7Path(segmentName, indices);
    }

    public static String segmentToString(HL7Segment segment, HL7Encoding encoding) {
        String fieldSeparator = String.valueOf(encoding.getFieldDelimiter());

        if (segment.name().equals(MSH_SEGMENT_NAME)) {
            return segment.name()
                    + segment.fields().get(0)
                    + String.join(
                            fieldSeparator, segment.fields().subList(1, segment.fields().size()));
        }
        return segment.name() + fieldSeparator + String.join(fieldSeparator, segment.fields());
    }
}
