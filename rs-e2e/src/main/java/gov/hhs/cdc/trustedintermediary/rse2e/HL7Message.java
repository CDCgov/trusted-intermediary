package gov.hhs.cdc.trustedintermediary.rse2e;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.List;
import java.util.Map;

/**
 * Represents a HAPI HL7 message that implements the HealthData interface. This class provides a
 * wrapper around the HAPI Message object.
 */
public class HL7Message implements HealthData<HL7Message> {

    private final Map<String, List<String>> segments;
    private final Map<String, Character> encodingCharacters;

    public HL7Message(
            Map<String, List<String>> segments, Map<String, Character> encodingCharacters) {
        this.segments = segments;
        this.encodingCharacters = encodingCharacters;
    }

    public List<String> getSegmentFields(String name) {
        return segments.get(name);
    }

    public String getValue(String segmentName, int... indices) {
        List<String> fields = getSegmentFields(segmentName);
        char[] levelDelimiters = this.getOrderedLevelDelimiters();
        return HL7Parser.parseAndGetValue(fields, levelDelimiters, indices);
    }

    public char getEncodingCharacter(String type) {
        return this.encodingCharacters.get(type);
    }

    public char getEscapeCharacter() {
        return getEncodingCharacter("escape");
    }

    public char[] getOrderedLevelDelimiters() {
        return new char[] {
            getEncodingCharacter("field"),
            getEncodingCharacter("component"),
            getEncodingCharacter("repetition"),
            getEncodingCharacter("subcomponent")
        };
    }

    @Override
    public HL7Message getUnderlyingData() {
        return this;
    }

    @Override
    public String getIdentifier() {
        return getValue("MSH", 10);
    }

    @Override
    public String toString() {
        return String.join("\n", segments.entrySet().stream().map(this::formatSegment).toList());
    }

    private String formatSegment(Map.Entry<String, List<String>> entry) {
        String name = entry.getKey();
        List<String> fields = entry.getValue();
        String fieldSeparator = String.valueOf(getEncodingCharacter("field"));

        return name
                + (name.equals("MSH") ? fields.get(0) : fieldSeparator)
                + String.join(
                        fieldSeparator,
                        name.equals("MSH") ? fields.subList(1, fields.size()) : fields);
    }
}
