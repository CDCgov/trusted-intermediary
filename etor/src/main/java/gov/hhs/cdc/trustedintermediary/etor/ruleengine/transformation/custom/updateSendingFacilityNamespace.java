package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import java.util.Collections;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

/**
 * Updates Sending Facility's Namespace Id (MSH-4) to given value and removes Universal Id (MSH-4.2)
 * and Universal Id Type (MSH-4.3).
 */
public class updateSendingFacilityNamespace implements CustomFhirTransformation {

    private final MetricMetadata metadata =
            ApplicationContext.getImplementation(MetricMetadata.class);

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        Organization sendingFacility = HapiHelper.getSendingFacility(bundle);
        Identifier namespaceIdentifier = HapiHelper.createHDNamespaceIdentifier();
        namespaceIdentifier.setValue(args.get("name"));
        sendingFacility.setIdentifier(Collections.singletonList(namespaceIdentifier));
        metadata.put(bundle.getId(), EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT);
    }
}
