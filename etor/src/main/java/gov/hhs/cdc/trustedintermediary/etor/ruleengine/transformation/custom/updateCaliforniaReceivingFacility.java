package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Collections;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

public class updateCaliforniaReceivingFacility implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        Organization receivingFacility = HapiHelper.getReceivingFacility(bundle);
        Identifier facilityIdentifier = HapiHelper.createHDNamespaceIdentifier();
        String orderingFacilityNameOrganizationIdentifier = "R797"; // Get it from ORC-21.10
        facilityIdentifier.setValue(orderingFacilityNameOrganizationIdentifier);
        receivingFacility.setIdentifier(Collections.singletonList(facilityIdentifier));
    }
}
