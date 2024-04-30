package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrderConverter;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;

public class convertToOrder implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();

        var overallId = UUID.randomUUID().toString();
        if (!bundle.hasId()) {
            bundle.setId(overallId);
        }

        if (!bundle.hasIdentifier()) {
            bundle.setIdentifier(new Identifier().setValue(overallId));
        }

        var orderDateTime = Date.from(Instant.now());
        if (!bundle.hasTimestamp()) {
            bundle.setTimestamp(orderDateTime);
        }

        bundle.setType(
                Bundle.BundleType.MESSAGE); // it always needs to be a message, so no if statement

        var patient = HapiOrderConverter.findPatientOrNull(bundle);

        var serviceRequest = HapiOrderConverter.createServiceRequest(patient, orderDateTime);
        var messageHeader = HapiOrderConverter.createOmlMessageHeader();
        var provenance = HapiOrderConverter.createProvenanceResource(orderDateTime);

        bundle.getEntry().add(0, new Bundle.BundleEntryComponent().setResource(messageHeader));
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest));
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(provenance));
    }
}
