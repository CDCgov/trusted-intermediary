package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.Map;

/**
 * Represents a custom transformation that can be applied to a FHIR resource. This interface is
 * implemented by classes in the custom/ folder.
 */
public interface CustomFhirTransformation {
    void transform(HealthData<?> resource, Map<String, String> args);
}
