package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import java.util.List;

/** The HL7Segment class represents an HL7 segment and its fields. */
public record HL7Segment(String name, List<String> fields) {}
