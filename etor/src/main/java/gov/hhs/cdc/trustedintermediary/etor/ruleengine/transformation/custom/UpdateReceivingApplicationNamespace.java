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
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;

/**
 * Updates Receiving Application's Namespace Id (MSH-5.1) to given value, and removes Universal Id
 * (MSH-5.2) and Universal Id Type (MSH-5.3).
 */
public class UpdateReceivingApplicationNamespace implements CustomFhirTransformation {

    private final MetricMetadata metadata =
            ApplicationContext.getImplementation(MetricMetadata.class);

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var header = HapiHelper.getMessageHeader(bundle);
        var organization = new Organization();
        organization.setName(args.get("name"));
        var destination = new MessageHeader.MessageDestinationComponent();
        destination.setReceiver(new Reference(organization));
        header.setDestination(Collections.singletonList(destination));
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(organization));
        metadata.put(bundle.getId(), EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT);
    }
}
