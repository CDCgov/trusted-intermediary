package gov.hhs.cdc.trustedintermediary.rse2e;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a HAPI HL7 message that implements the HealthData interface. This class provides a
 * wrapper around the HAPI Message object.
 */
public class HL7Message implements HealthData<HL7Message> {

    private final Map<String, List<String>> segments;
    private final char[] delimiters;

    public HL7Message(Map<String, List<String>> segments, char[] delimiters) {
        this.segments = segments;
        this.delimiters = delimiters;
    }

    public List<String> getSegment(String segment) {
        return segments.get(segment);
    }

    public String getValue(String segmentName, int... indices) {
        List<String> fields = segments.get(segmentName);
        if (fields == null || indices[0] > fields.size()) {
            return null;
        }

        String value = fields.get(indices[0] - 1);
        for (int i = 1; i < indices.length; i++) {
            char levelDelimiter = delimiters[i - 1];
            int index = indices[i] - 1;
            String[] parts = value.split(Pattern.quote(String.valueOf(levelDelimiter)));
            if (index < 0 || index >= parts.length) {
                return null;
            }
            value = parts[index];
        }
        return value;
    }

    @Override
    public HL7Message getUnderlyingData() {
        return this;
    }

    @Override
    public String getIdentifier() {
        return getValue("MSH", 10);
    }
}
