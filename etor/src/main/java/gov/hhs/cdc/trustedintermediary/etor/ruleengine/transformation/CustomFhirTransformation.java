package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import java.util.Map;

/**
 * Represents a custom transformation that can be applied to a FHIR resource. This interface is
 * implemented by classes in the custom/ folder.
 */
public interface CustomFhirTransformation {
    void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException;
}
