package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ServiceRequest;

/**
 * Updates Placer Order Number in ORC and OBR (ORC-2 and OBR-4) with content from Placer Group
 * Number (ORC-4). Specifically, it updated ORC-2.1/OBR-2.1 with OBR-4.1, and ORC-2.2/OBR-2.2 with
 * OBR-4.2. It also updates Placer Group Number (ORC-4) with the original value for Placer Order
 * Number (ORC-2). Specifically, it updates ORC-4.1 and ORC-4.2 with the original values in ORC-2.1
 * and ORC-2.2 respectively.
 */
public class SwapPlacerOrderAndGroupNumbers implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

        for (ServiceRequest serviceRequest : serviceRequests.toList()) {
            String orc2_1 = HapiHelper.getORC2_1Value(serviceRequest);
            String orc2_2 = HapiHelper.getORC2_2Value(serviceRequest);
            String orc4_1 = HapiHelper.getORC4_1Value(serviceRequest);
            String orc4_2 = HapiHelper.getORC4_2Value(serviceRequest);

            HapiHelper.setORC2_1Value(serviceRequest, orc4_1);
            HapiHelper.setORC2_2Value(serviceRequest, orc4_2);
            HapiHelper.setOBR2_1Value(serviceRequest, orc4_1);
            HapiHelper.setOBR2_2Value(serviceRequest, orc4_2);
            HapiHelper.setORC4_1Value(serviceRequest, orc2_1);
            HapiHelper.setORC4_2Value(serviceRequest, orc2_2);
        }
    }
}
