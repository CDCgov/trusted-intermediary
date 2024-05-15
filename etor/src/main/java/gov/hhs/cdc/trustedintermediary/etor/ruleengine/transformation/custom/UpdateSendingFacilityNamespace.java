package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Collections;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;

/**
 * Updates Sending Facility's Namespace Id (MSH-4) to given value and removes Universal Id (MSH-4.2)
 * and Universal Id Type (MSH-4.3).
 */
public class UpdateSendingFacilityNamespace implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        Identifier namespaceIdentifier = HapiHelper.getSendingFacilityNamespace(bundle);
        namespaceIdentifier.setValue(args.get("name"));
        HapiHelper.getSendingFacility(bundle)
                .setIdentifier(Collections.singletonList(namespaceIdentifier));
    }
}
