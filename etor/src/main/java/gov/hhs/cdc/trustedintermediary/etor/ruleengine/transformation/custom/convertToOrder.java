package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiMessageConverterHelper;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.UrlType;

public class convertToOrder implements CustomFhirTransformation {

    private static final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");

    @Override
    public FhirResource<?> transform(
            final FhirResource<?> resource, final Map<String, String> args) {
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

        var patient = HapiMessageConverterHelper.findPatientOrNull(bundle);

        var serviceRequest = createServiceRequest(patient, orderDateTime);
        var messageHeader = createMessageHeader();
        var provenance = createProvenanceResource(orderDateTime);

        bundle.getEntry().add(0, new Bundle.BundleEntryComponent().setResource(messageHeader));
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest));
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(provenance));

        return resource;
    }

    private MessageHeader createMessageHeader() {
        var messageHeader = new MessageHeader();

        messageHeader.setId(UUID.randomUUID().toString());

        messageHeader.setEvent(OML_CODING);

        var meta = new Meta();

        // Adding processing id of 'P'
        meta.addTag("http://terminology.hl7.org/CodeSystem/v2-0103", "P", "Production");

        messageHeader.setMeta(meta);

        messageHeader.setSource(
                new MessageHeader.MessageSourceComponent(
                                new UrlType("https://reportstream.cdc.gov/"))
                        .setName("CDC Trusted Intermediary"));

        return messageHeader;
    }

    private ServiceRequest createServiceRequest(final Patient patient, final Date orderDateTime) {
        var serviceRequest = new ServiceRequest();

        serviceRequest.setId(UUID.randomUUID().toString());

        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);

        serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);

        serviceRequest.setCode(
                new CodeableConcept(
                        new Coding("http://loinc.org", "54089-8", "Newborn Screening Panel")));

        serviceRequest.addCategory(
                new CodeableConcept(
                        new Coding("http://snomed.info/sct", "108252007", "Laboratory procedure")));

        serviceRequest.setSubject(new Reference(patient));

        serviceRequest.setAuthoredOn(orderDateTime);

        return serviceRequest;
    }

    private Provenance createProvenanceResource(Date orderDate) {
        var provenance = new Provenance();

        provenance.setId(UUID.randomUUID().toString());
        provenance.setRecorded(orderDate);
        provenance.setActivity(new CodeableConcept(OML_CODING));

        return provenance;
    }
}
