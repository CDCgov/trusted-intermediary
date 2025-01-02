package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a HL7 message that implements the HealthData interface and adds methods to access the
 * HL7 data
 */
public class HL7Message implements HealthData<HL7Message> {

    static final String MSH_SEGMENT_NAME = "MSH";
    static final String NEWLINE_REGEX = "\\r?\\n|\\r";

    private final List<HL7Segment> segments;
    private final HL7Encoding encoding;

    HL7Message(List<HL7Segment> segments, HL7Encoding encoding) {
        this.segments = segments;
        this.encoding = encoding;
    }

    @Override
    public HL7Message getUnderlyingData() {
        return this;
    }

    @Override
    public String getIdentifier() {
        try {
            return getValue("MSH-10");
        } catch (HL7MessageException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.join(
                HL7Encoding.DEFAULT_SEGMENT_DELIMITER,
                getSegments().stream()
                        .map(segment -> segmentToString(segment, this.encoding))
                        .toList());
    }

    public static HL7Message parse(String content) {
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

    public List<HL7Segment> getSegments() {
        return this.segments;
    }

    public HL7Encoding getEncoding() {
        return this.encoding;
    }

    public List<HL7Segment> getSegments(String name) {
        return this.segments.stream().filter(segment -> segment.name().equals(name)).toList();
    }

    public int getSegmentCount(String name) {
        return getSegments(name).size();
    }

    public boolean hasSegment(String name, int index) {
        return getSegmentCount(name) > index;
    }

    public HL7Segment getSegment(String name, int index) throws HL7MessageException {
        if (!hasSegment(name, index)) {
            throw new HL7MessageException(
                    String.format("Segment %s at index %d not found", name, index));
        }
        return getSegments(name).get(index);
    }

    public HL7Segment getSegment(String name) throws HL7MessageException {
        return getSegment(name, 0);
    }

    public String getValue(String path) throws HL7MessageException {
        HL7Path hl7Path = HL7Path.parse(path);
        int[] indices = hl7Path.indices();
        List<String> fields = this.getSegment(hl7Path.segmentName()).fields();
        char[] delimiters = this.getEncoding().getOrderedDelimiters();

        if (indices[0] > fields.size()) {
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

    protected static String segmentToString(HL7Segment segment, HL7Encoding encoding) {
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
