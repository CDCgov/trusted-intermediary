package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** The HL7Parser class is responsible for parsing HL7 messages and extracting values from them. */
public class HL7Parser {
    static final String MSH_SEGMENT_NAME = "MSH";
    static final String NEWLINE_REGEX = "\\r?\\n|\\r";

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
