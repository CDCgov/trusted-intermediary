package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

public class MultiplyObservationValue implements CustomFhirTransformation {

    @Override
    public void transform(HealthData<?> resource, Map<String, Object> args) {
        var bundle = (Bundle) resource.getUnderlyingData();
    }
}
