package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import java.util.Map;

public interface CustomFhirTransformation {
    void transform(FhirResource<?> resource, Map<String, String> args);
}
