package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

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

    public int getSegmentCount(String name) {
        var matches = getSegmentFields(name);
        if (matches != null) {
            return matches.size();
        }
        return 0;
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
        return getEncodingCharacter(HL7Parser.ESCAPE_CHARACTER_NAME);
    }

    public char[] getOrderedLevelDelimiters() {
        return new char[] {
            getEncodingCharacter(HL7Parser.FIELD_DELIMITER_NAME),
            getEncodingCharacter(HL7Parser.COMPONENT_DELIMITER_NAME),
            getEncodingCharacter(HL7Parser.REPETITION_DELIMITER_NAME),
            getEncodingCharacter(HL7Parser.SUBCOMPONENT_DELIMITER_NAME)
        };
    }

    @Override
    public HL7Message getUnderlyingData() {
        return this;
    }

    @Override
    public String getIdentifier() {
        return getValue(HL7Parser.MSH_SEGMENT_NAME, 10);
    }

    @Override
    public String toString() {
        return String.join(
                HL7Parser.DEFAULT_SEGMENT_DELIMITER,
                segments.entrySet().stream().map(this::formatSegment).toList());
    }

    private String formatSegment(Map.Entry<String, List<String>> entry) {
        String name = entry.getKey();
        List<String> fields = entry.getValue();
        String fieldSeparator =
                String.valueOf(getEncodingCharacter(HL7Parser.FIELD_DELIMITER_NAME));

        return name
                + (name.equals(HL7Parser.MSH_SEGMENT_NAME) ? fields.get(0) : fieldSeparator)
                + String.join(
                        fieldSeparator,
                        name.equals(HL7Parser.MSH_SEGMENT_NAME)
                                ? fields.subList(1, fields.size())
                                : fields);
    }
}
