package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

/** Removes Name Type Code (PID-5.7) from Patient Name (PID-5). */
public class RemovePatientNameTypeCode implements CustomFhirTransformation {

    @Override
    public void transform(final HealthData<?> resource, final Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingData();
        // Need to set the value for extension to empty instead of removing the extension,
        // otherwise RS will set its own value in its place
        HapiHelper.setPID5_7ExtensionValue(bundle, null);
    }
}
