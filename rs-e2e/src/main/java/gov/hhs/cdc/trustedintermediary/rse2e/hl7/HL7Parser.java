package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class HL7Parser {
    private static final String NEWLINE_REGEX = "\\r?\\n|\\r";
    private static final char DEFAULT_FIELD_DELIMITER = '|';
    private static final char DEFAULT_COMPONENT_DELIMITER = '^';
    private static final char DEFAULT_REPETITION_DELIMITER = '~';
    private static final char DEFAULT_ESCAPE_CHARACTER = '\\';
    private static final char DEFAULT_SUBCOMPONENT_DELIMITER = '&';
    private static final char[] DEFAULT_ENCODING_CHARACTERS =
            new char[] {
                DEFAULT_COMPONENT_DELIMITER,
                DEFAULT_REPETITION_DELIMITER,
                DEFAULT_ESCAPE_CHARACTER,
                DEFAULT_SUBCOMPONENT_DELIMITER
            };
    protected static final String FIELD_DELIMITER_NAME = "field";
    protected static final String COMPONENT_DELIMITER_NAME = "component";
    protected static final String REPETITION_DELIMITER_NAME = "repetition";
    protected static final String ESCAPE_CHARACTER_NAME = "escape";
    protected static final String SUBCOMPONENT_DELIMITER_NAME = "subcomponent";
    protected static final String MSH_SEGMENT_NAME = "MSH";
    protected static final String DEFAULT_SEGMENT_DELIMITER = "\n";
    protected static final Pattern HL7_FIELD_NAME_PATTERN = Pattern.compile("(\\w+)(?:-(\\S+))?");

    public static HL7Message parse(String content) {
        List<HL7Segment> segments = new ArrayList<>();
        String encodingCharactersField = null;
        String[] lines = content.split(NEWLINE_REGEX);
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] fields = line.split(Pattern.quote(String.valueOf(DEFAULT_FIELD_DELIMITER)));
            String segmentName = fields[0];
            List<String> segmentFields =
                    new ArrayList<>(Arrays.asList(fields).subList(1, fields.length));
            if (Objects.equals(segmentName, MSH_SEGMENT_NAME)) {
                encodingCharactersField = fields[1];
                segmentFields.add(0, String.valueOf(DEFAULT_FIELD_DELIMITER));
            }
            segments.add(new HL7Segment(segmentName, segmentFields));
        }

        return new HL7Message(segments, getEncodingCharacters(encodingCharactersField));
    }

    public static String parseAndGetValue(List<String> fields, char[] delimiters, int... indices) {
        if (fields == null || indices[0] > fields.size()) {
            return null;
        }

        String value = fields.get(indices[0] - 1);
        for (int i = 1; i < indices.length; i++) {
            if (i >= delimiters.length) {
                return null;
            }
            char levelDelimiter = delimiters[i];
            int index = indices[i] - 1;
            String[] parts = value.split(Pattern.quote(String.valueOf(levelDelimiter)));
            if (index < 0 || index >= parts.length) {
                return null;
            }
            value = parts[index];
        }
        return value;
    }

    public static Map<String, Character> getEncodingCharacters(String encodingCharactersField) {
        char[] encodingCharacters = DEFAULT_ENCODING_CHARACTERS;
        if (encodingCharactersField != null && !encodingCharactersField.isEmpty()) {
            encodingCharacters = encodingCharactersField.toCharArray();
        }

        return Map.of(
                FIELD_DELIMITER_NAME, HL7Parser.DEFAULT_FIELD_DELIMITER,
                COMPONENT_DELIMITER_NAME,
                        encodingCharacters.length > 0
                                ? encodingCharacters[0]
                                : HL7Parser.DEFAULT_COMPONENT_DELIMITER,
                REPETITION_DELIMITER_NAME,
                        encodingCharacters.length > 1
                                ? encodingCharacters[1]
                                : HL7Parser.DEFAULT_REPETITION_DELIMITER,
                ESCAPE_CHARACTER_NAME,
                        encodingCharacters.length > 2
                                ? encodingCharacters[2]
                                : HL7Parser.DEFAULT_ESCAPE_CHARACTER,
                SUBCOMPONENT_DELIMITER_NAME,
                        encodingCharacters.length > 3
                                ? encodingCharacters[3]
                                : HL7Parser.DEFAULT_SUBCOMPONENT_DELIMITER);
    }
}
