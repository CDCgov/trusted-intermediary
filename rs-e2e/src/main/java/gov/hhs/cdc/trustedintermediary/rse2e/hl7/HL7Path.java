package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

/** The HL7Path class represents a path to a specific field in an HL7 message. */
public record HL7Path(String segmentName, int[] indices) {}
