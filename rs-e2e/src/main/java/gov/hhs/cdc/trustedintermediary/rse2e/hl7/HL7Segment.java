package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import java.util.List;

public record HL7Segment(String name, List<String> fields) {}
