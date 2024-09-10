package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import java.util.Map;

/**
 * Represents a custom transformation that can be applied to a FHIR resource. This interface is
 * implemented by classes in the custom/ folder.
 */
public interface CustomFhirTransformation {
    void transform(HL7Message<?> resource, Map<String, String> args);
}
