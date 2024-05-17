package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ServiceRequest;

/**
 * Updates Placer Order Number (ORC-2) with content from Placer Group Number (ORC-4). It also
 * replaces Placer Order Number's Entity Identifier (ORC-2.1) and Namespace Id (ORC-2.2) with Placer
 * Group Number's Entity Identifier (ORC-4.1) and Namespace Id (ORC-4.2) respectively. Effectively,
 * we're swapping ORC-2 for ORC-4 and vice versa.
 */
public class SwapPlacerOrderAndGroupNumbers implements CustomFhirTransformation {
    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException {
        try {
            Bundle bundle = (Bundle) resource.getUnderlyingResource();
            var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

            for (ServiceRequest serviceRequest : serviceRequests.toList()) {
                Identifier placerOrderNumberIdentifier =
                        HapiHelper.getPlacerOrderNumberIdentifier(serviceRequest);
                String orc2_1 = HapiHelper.getEI1Value(placerOrderNumberIdentifier);
                String orc2_2 = HapiHelper.getEI2Value(placerOrderNumberIdentifier);
                Coding placerGroupNumberCoding =
                        HapiHelper.getPlacerGroupNumberCoding(serviceRequest);
                String orc4_1 = placerGroupNumberCoding.getCode();
                String orc4_2 = placerGroupNumberCoding.getDisplay();

                HapiHelper.setEI1Value(placerOrderNumberIdentifier, orc4_1);
                HapiHelper.setEI2Value(placerOrderNumberIdentifier, orc4_2);
                placerGroupNumberCoding.setCode(orc2_1);
                placerGroupNumberCoding.setDisplay(orc2_2);
            }
        } catch (Exception e) {
            throw new RuleExecutionException("Failed to switch placer order and group numbers", e);
        }
    }
}
