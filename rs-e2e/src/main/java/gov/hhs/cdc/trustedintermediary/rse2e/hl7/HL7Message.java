package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Represents a HAPI HL7 message that implements the HealthData interface. This class provides a
 * wrapper around the HAPI Message object.
 */
public class HL7Message implements HealthData<HL7Message> {

    private final List<HL7Segment> segments;
    private final Map<String, Character> encodingCharacters;

    HL7Message(List<HL7Segment> segments, Map<String, Character> encodingCharacters) {
        this.segments = segments;
        this.encodingCharacters = encodingCharacters;
    }

    public List<HL7Segment> getSegments(String name) {
        return segments.stream().filter(segment -> segment.name().equals(name)).toList();
    }

    public HL7Segment getSegment(String name, int index) {
        return getSegments(name).get(index);
    }

    public HL7Segment getSegment(String name) {
        return getSegments(name).get(0);
    }

    public List<String> getSegmentFields(String name, int index) {
        return getSegment(name, index).fields();
    }

    public List<String> getSegmentFields(String name) {
        return getSegment(name, 0).fields();
    }

    public int getSegmentCount(String name) {
        return getSegments(name).size();
    }

    public String getValue(String hl7Path) {
        Matcher hl7FieldNameMatcher = HL7Parser.HL7_FIELD_NAME_PATTERN.matcher(hl7Path);
        if (!hl7FieldNameMatcher.matches()) {
            throw new IllegalArgumentException("Invalid HL7 path format: " + hl7Path);
        }

        String segmentName = hl7FieldNameMatcher.group(1);
        String segmentFieldIndex = hl7FieldNameMatcher.group(2);
        int[] indexParts =
                Arrays.stream(segmentFieldIndex.split("\\.")).mapToInt(Integer::parseInt).toArray();
        return getValue(segmentName, indexParts);
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
                segments.stream().map(this::segmentToString).toList());
    }

    private String segmentToString(HL7Segment segment) {
        String fieldSeparator =
                String.valueOf(getEncodingCharacter(HL7Parser.FIELD_DELIMITER_NAME));

        if (segment.name().equals(HL7Parser.MSH_SEGMENT_NAME)) {
            return segment.name()
                    + segment.fields().get(0)
                    + String.join(
                            fieldSeparator, segment.fields().subList(1, segment.fields().size()));
        }
        return segment.name() + fieldSeparator + String.join(fieldSeparator, segment.fields());
    }
}
