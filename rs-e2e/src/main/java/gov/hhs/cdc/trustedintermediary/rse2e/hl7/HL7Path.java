package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import java.util.Arrays;
import java.util.Objects;

/** The HL7Path class represents a path to a specific field in an HL7 message. */
public record HL7Path(String segmentName, int[] indices) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HL7Path hl7Path = (HL7Path) o;
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
