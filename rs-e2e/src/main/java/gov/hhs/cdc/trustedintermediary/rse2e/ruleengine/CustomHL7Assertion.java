package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a custom assertion that can be applied to a FHIR resource. This interface is
 * implemented by classes in the custom/ folder.
 */
public interface CustomHL7Assertion {
    void transform(HL7Message<?> resource, Map<String, ArrayList<?>> args);
}
