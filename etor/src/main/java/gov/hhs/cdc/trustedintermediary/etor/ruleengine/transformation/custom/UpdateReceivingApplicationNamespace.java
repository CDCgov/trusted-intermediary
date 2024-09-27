package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Updates Receiving Application's Namespace Id (MSH-5.1) to given value, and removes Universal Id
 * (MSH-5.2) and Universal Id Type (MSH-5.3).
 */
public class UpdateReceivingApplicationNamespace implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        String name = (args.get("name") instanceof String ? (String) args.get("name") : null);
        var receivingApplication = HapiHelper.getMSH5MessageDestinationComponent(bundle);
        if (name == null) {
            return;
        }
        if (receivingApplication == null) {
            return;
        }

        receivingApplication.removeExtension(HapiHelper.EXTENSION_UNIVERSAL_ID_URL);
        receivingApplication.removeExtension(HapiHelper.EXTENSION_UNIVERSAL_ID_TYPE_URL);
        receivingApplication.setName(name);
    }
}
