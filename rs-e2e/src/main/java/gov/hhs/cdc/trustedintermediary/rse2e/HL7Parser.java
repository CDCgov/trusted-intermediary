package gov.hhs.cdc.trustedintermediary.rse2e;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class HL7Parser {

    private static final HL7Parser INSTANCE = new HL7Parser();

    private static final String NEWLINE_REGEX = "\\r?\\n|\\r";
    private static final char DEFAULT_FIELD_SEPARATOR = '|';

    private HL7Parser() {}

    public static HL7Parser getInstance() {
        return INSTANCE;
    }

    public static HL7Message parse(String content) {
        Map<String, List<String>> segments = new HashMap<>();
        char[] encodingCharacters = null;
        String[] lines = content.split(NEWLINE_REGEX);
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] fields = line.split(Pattern.quote(String.valueOf(DEFAULT_FIELD_SEPARATOR)));
            String segmentName = fields[0];
            List<String> segmentFields =
                    new ArrayList<>(Arrays.asList(fields).subList(1, fields.length));
            if (Objects.equals(segmentName, "MSH")) {
                encodingCharacters = fields[1].toCharArray();
                segmentFields.add(0, String.valueOf(DEFAULT_FIELD_SEPARATOR));
            }
            segments.put(segmentName, segmentFields);
        }
        return new HL7Message(segments, encodingCharacters);
    }
}
