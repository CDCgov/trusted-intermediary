package gov.hhs.cdc.trustedintermediary.rse2e;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class HL7Parser {
    protected static final String NEWLINE_REGEX = "\\r?\\n|\\r";
    protected static final char DEFAULT_FIELD_DELIMITER = '|';
    protected static final char DEFAULT_COMPONENT_DELIMITER = '^';
    protected static final char DEFAULT_REPETITION_DELIMITER = '~';
    protected static final char DEFAULT_ESCAPE_CHARACTER = '\\';
    protected static final char DEFAULT_SUBCOMPONENT_DELIMITER = '&';
    private static final char[] DEFAULT_ENCODING_CHARACTERS =
            new char[] {
                DEFAULT_COMPONENT_DELIMITER,
                DEFAULT_REPETITION_DELIMITER,
                DEFAULT_ESCAPE_CHARACTER,
                DEFAULT_SUBCOMPONENT_DELIMITER
            };

    public static HL7Message parse(String content) {
        Map<String, List<String>> segments = new HashMap<>();
        Map<String, Character> encodingCharacters = new HashMap<>();
        String[] lines = content.split(NEWLINE_REGEX);
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] fields = line.split(Pattern.quote(String.valueOf(DEFAULT_FIELD_DELIMITER)));
            String segmentName = fields[0];
            List<String> segmentFields =
                    new ArrayList<>(Arrays.asList(fields).subList(1, fields.length));
            if (Objects.equals(segmentName, "MSH")) {
                encodingCharacters = getEncodingCharacters(fields[1]);
                segmentFields.add(0, String.valueOf(DEFAULT_FIELD_DELIMITER));
            }
            segments.put(segmentName, segmentFields);
        }

        return new HL7Message(segments, encodingCharacters);
    }

    private static Map<String, Character> getEncodingCharacters(String encodingCharactersField) {
        char[] encodingCharacters = DEFAULT_ENCODING_CHARACTERS;
        if (encodingCharactersField != null && !encodingCharactersField.isEmpty()) {
            encodingCharacters = encodingCharactersField.toCharArray();
        }

        return Map.of(
                "field", HL7Parser.DEFAULT_FIELD_DELIMITER,
                "component",
                        encodingCharacters.length > 0
                                ? encodingCharacters[0]
                                : HL7Parser.DEFAULT_COMPONENT_DELIMITER,
                "repetition",
                        encodingCharacters.length > 1
                                ? encodingCharacters[1]
                                : HL7Parser.DEFAULT_REPETITION_DELIMITER,
                "escape",
                        encodingCharacters.length > 2
                                ? encodingCharacters[2]
                                : HL7Parser.DEFAULT_ESCAPE_CHARACTER,
                "subcomponent",
                        encodingCharacters.length > 3
                                ? encodingCharacters[3]
                                : HL7Parser.DEFAULT_SUBCOMPONENT_DELIMITER);
    }
}
