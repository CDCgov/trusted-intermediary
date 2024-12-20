package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.List;

/**
 * Represents a HL7 message that implements the HealthData interface and adds methods to access the
 * HL7 data
 */
public class HL7Message implements HealthData<HL7Message> {

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
                        .map(segment -> HL7Parser.segmentToString(segment, this.encoding))
                        .toList());
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
        HL7Path hl7Path = HL7Parser.parsePath(path);
        return HL7Parser.parseMessageFieldValue(hl7Path, this);
    }
}
