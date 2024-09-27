package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;

/**
 * Updates Sending Facility's Namespace Id (MSH-4) to given value and removes Universal Id (MSH-4.2)
 * and Universal Id Type (MSH-4.3).
 */
public class UpdateSendingFacilityNamespace implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        Identifier namespaceIdentifier = HapiHelper.getMSH4_1Identifier(bundle);
        if (namespaceIdentifier == null) {
            return;
        }

        String name = args.get("name") instanceof String ? (String) args.get("name") : null;
        if (name == null) {
            return;
        }

        namespaceIdentifier.setValue(name);
        Objects.requireNonNull(HapiHelper.getMSH4Organization(bundle))
                .setIdentifier(Collections.singletonList(namespaceIdentifier));
    }
}
