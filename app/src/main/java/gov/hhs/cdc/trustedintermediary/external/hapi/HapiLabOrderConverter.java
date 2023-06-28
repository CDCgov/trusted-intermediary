package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrderConverter;
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
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
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

        var orderDateTime = Date.from(Instant.now());
        if (!demographicsBundle.hasTimestamp()) {
            demographicsBundle.setTimestamp(orderDateTime);
        }

        demographicsBundle.setType(
                Bundle.BundleType.MESSAGE); // it always needs to be a message, so no if statement

        var patient =
                HapiHelper.resourcesInBundle(demographicsBundle, Patient.class)
                        .findFirst()
                        .orElse(null);
        var omlLabOrderCoding =
                new Coding(
                        "http://terminology.hl7.org/CodeSystem/v2-0003",
                        "O21",
                        "OML - Laboratory order");

        var serviceRequest = createServiceRequest(patient, orderDateTime);
        var messageHeader = createMessageHeader(omlLabOrderCoding);
        var provenance = createProvenanceResource(orderDateTime, omlLabOrderCoding);

        demographicsBundle
                .getEntry()
                .add(0, new Bundle.BundleEntryComponent().setResource(messageHeader));
        demographicsBundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest));
        demographicsBundle.addEntry(new Bundle.BundleEntryComponent().setResource(provenance));

        return new HapiLabOrder(demographicsBundle);
    }

    private MessageHeader createMessageHeader(Coding omlLabOrderCoding) {
        logger.logInfo("Creating new MessageHeader");

        var messageHeader = new MessageHeader();

        messageHeader.setId(UUID.randomUUID().toString());

        messageHeader.setEvent(omlLabOrderCoding);

        messageHeader.setMeta(
                new Meta()
                        .addTag(
                                new Coding(
                                        "http://terminology.hl7.org/CodeSystem/v2-0103",
                                        "P",
                                        "Production")));

        messageHeader.setSource(
                new MessageHeader.MessageSourceComponent(
                                new UrlType("https://reportstream.cdc.gov/"))
                        .setName("CDC Trusted Intermediary"));

        return messageHeader;
    }

    private ServiceRequest createServiceRequest(final Patient patient, final Date orderDateTime) {
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

        serviceRequest.setAuthoredOn(orderDateTime);

        return serviceRequest;
    }

    private Provenance createProvenanceResource(Date orderDate, Coding omlLabOrderCoding) {
        logger.logInfo("Creating new Provenance");
        var provenance = new Provenance();

        provenance.setId(UUID.randomUUID().toString());
        provenance.setRecorded(orderDate);
        provenance.setActivity(new CodeableConcept(omlLabOrderCoding));

        return provenance;
    }
}
