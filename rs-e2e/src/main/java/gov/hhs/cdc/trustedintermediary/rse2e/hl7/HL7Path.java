package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The HL7Path class represents a path to a specific field in an HL7 message. */
public record HL7Path(String segmentName, int[] indices) {
    static final Pattern HL7_FIELD_NAME_PATTERN = Pattern.compile("(\\w+)-(\\d+(?:\\.\\d+)*)");

    public static HL7Path parse(String path) {
        Matcher matcher = HL7_FIELD_NAME_PATTERN.matcher(path);
        if (!matcher.matches()) {
            throw new HL7ParserException("Invalid HL7 path format: " + path);
        }

        String segmentName = matcher.group(1);
        int[] indices =
                Arrays.stream(matcher.group(2).split("\\.")).mapToInt(Integer::parseInt).toArray();

        return new HL7Path(segmentName, indices);
    }

    // Need to override equals, hashCode, and toString to handle array comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HL7Path hl7Path)) return false;
        return (Objects.equals(segmentName, hl7Path.segmentName))
                && Arrays.equals(indices, hl7Path.indices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segmentName, Arrays.hashCode(indices));
    }

    @Override
    public String toString() {
        return "HL7Path[segmentName=" + segmentName + ", indices=" + Arrays.toString(indices) + "]";
    }
}
