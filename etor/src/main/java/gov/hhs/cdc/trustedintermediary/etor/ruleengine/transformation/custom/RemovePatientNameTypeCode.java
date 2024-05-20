package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

/** Removes Name Type Code (PID-5.7) from Patient Name (PID-5). */
public class RemovePatientNameTypeCode implements CustomFhirTransformation {

    @Override
    public void transform(final FhirResource<?> resource, final Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        HapiHelper.removePID5_7Extension(bundle);
    }
}
