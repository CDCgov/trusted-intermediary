package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;

/**
 * Removes Assigning Authority (PID-3.4) and Identifier Type Code (PID-3.5) from Patient Identifier
 * List (PID-3).
 */
public class RemovePatientIdentifiers implements CustomFhirTransformation {

    @Override
    public void transform(HealthData<?> resource, Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingData();

        Identifier identifier = HapiHelper.getPID3Identifier(bundle);
        if (identifier == null) {
            return;
        }
        identifier.setAssigner(null);

        if (identifier.hasExtension(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)) {
            identifier
                    .getExtensionByUrl(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)
                    .removeExtension(HapiHelper.EXTENSION_CX5_URL);
        }

        if (identifier
                .getExtensionByUrl(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)
                .getExtension()
                .isEmpty()) {
            identifier.removeExtension(HapiHelper.EXTENSION_CX_IDENTIFIER_URL);
        }

        identifier.setType(null);
    }
}
