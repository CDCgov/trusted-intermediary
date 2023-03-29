package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderConverter;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.UrlType;

/**
 * Converts {@link Demographics} to a Hapi-specific FHIR lab order ({@link HapiLabOrder} or {@link
 * LabOrder<Bundle>}).
 */
public class HapiLabOrderConverter implements LabOrderConverter {
    private static final HapiLabOrderConverter INSTANCE = new HapiLabOrderConverter();

    @Inject Logger logger;

    public static HapiLabOrderConverter getInstance() {
        return INSTANCE;
    }

    private HapiLabOrderConverter() {}

    @Override
    public HapiLabOrder convertToOrder(final Demographics<?> demographics) {
        logger.logInfo("Converting demographics to order");

        var hapiDemographics = (Demographics<Bundle>) demographics;
        var demographicsBundle = hapiDemographics.getUnderlyingDemographics();

        var overallId = UUID.randomUUID().toString();
        if (!demographicsBundle.hasId()) {
            demographicsBundle.setId(overallId);
        }

        if (!demographicsBundle.hasIdentifier()) {
            demographicsBundle.setIdentifier(new Identifier().setValue(overallId));
        }

        if (!demographicsBundle.hasTimestamp()) {
            demographicsBundle.setTimestamp(Date.from(Instant.now()));
        }

        demographicsBundle.setType(
                Bundle.BundleType.MESSAGE); // it always needs to be a message, so no if statement

        var patient =
                demographicsBundle.getEntry().stream()
                        .map(Bundle.BundleEntryComponent::getResource)
                        .filter(resource -> resource.getResourceType().equals(ResourceType.Patient))
                        .map(resource -> ((Patient) resource))
                        .findFirst()
                        .orElse(null);

        var serviceRequest = createServiceRequest(patient);
        var messageHeader = createMessageHeader();

        demographicsBundle
                .getEntry()
                .add(0, new Bundle.BundleEntryComponent().setResource(messageHeader));
        demographicsBundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest));

        return new HapiLabOrder(demographicsBundle);
    }

    private MessageHeader createMessageHeader() {
        logger.logInfo("Creating new MessageHeader");

        var messageHeader = new MessageHeader();

        messageHeader.setId(UUID.randomUUID().toString());
        messageHeader.setEvent(
                new Coding(
                        "http://terminology.hl7.org/CodeSystem/v2-0003",
                        "O21",
                        "OML - Laboratory order"));
        messageHeader.setSource(
                new MessageHeader.MessageSourceComponent(
                                new UrlType("https://reportstream.cdc.gov/"))
                        .setName("CDC Trusted Intermediary"));

        return messageHeader;
    }

    private ServiceRequest createServiceRequest(final Patient patient) {
        logger.logInfo("Creating new ServiceRequest");

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

        serviceRequest.setAuthoredOn(Date.from(Instant.now()));

        return serviceRequest;
    }
}
