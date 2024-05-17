package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ServiceRequest;

/**
 * Updates Placer Order Number (ORC-2) with content from Placer Group Number (ORC-4). It also
 * replaces Placer Order Number's Entity Identifier (ORC-2.1) and Namespace Id (ORC-2.2) with Placer
 * Group Number's Entity Identifier (ORC-4.1) and Namespace Id (ORC-4.2) respectively. Effectively,
 * we're swapping ORC-2 for ORC-4 and vice versa.
 */
public class SwapPlacerOrderAndGroupNumbers implements CustomFhirTransformation {
    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

        for (ServiceRequest serviceRequest : serviceRequests.toList()) {
            String orc2_1 = HapiHelper.getORC2_1Value(serviceRequest);
            String orc2_2 = HapiHelper.getORC2_2Value(serviceRequest);
            String orc4_1 = HapiHelper.getORC4_1Value(serviceRequest);
            String orc4_2 = HapiHelper.getORC4_2Value(serviceRequest);

            HapiHelper.setORC2_1Value(serviceRequest, orc4_1);
            HapiHelper.setORC2_2Value(serviceRequest, orc4_2);
            HapiHelper.setORC4_1Value(serviceRequest, orc2_1);
            HapiHelper.setORC4_2Value(serviceRequest, orc2_2);
        }
    }
}
