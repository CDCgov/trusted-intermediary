package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.Map;

public interface CustomFhirTransformation {
    FhirResource<?> transform(FhirResource<?> resource, Map<String, String> args);
}
