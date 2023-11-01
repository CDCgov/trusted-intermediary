package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;
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
 * Converts {@link Demographics} to a Hapi-specific FHIR lab order ({@link HapiOrder} or {@link
 * Order <Bundle>}). Also converts an order to identify as an HL7v2 OML in the {@link
 * MessageHeader}.
 */
public class HapiOrderConverter implements OrderConverter {
    private static final HapiOrderConverter INSTANCE = new HapiOrderConverter();
    private static final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");

    private static final List<Coding> CODING_LIST =
            List.of(
                    new Coding(
                            "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH", "mother"));

    @Inject Logger logger;

    public static HapiOrderConverter getInstance() {
        return INSTANCE;
    }

    private HapiOrderConverter() {}

    @Override
    public HapiOrder convertToOrder(final Demographics<?> demographics) {
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

        var serviceRequest = createServiceRequest(patient, orderDateTime);
        var messageHeader = createMessageHeader();
        var provenance = createProvenanceResource(orderDateTime);

        demographicsBundle
                .getEntry()
                .add(0, new Bundle.BundleEntryComponent().setResource(messageHeader));
        demographicsBundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest));
        demographicsBundle.addEntry(new Bundle.BundleEntryComponent().setResource(provenance));

        return new HapiOrder(demographicsBundle);
    }

    @Override
    public Order<?> convertMetadataToOmlOrder(Order<?> order) {
        logger.logInfo("Converting order to have OML metadata");

        var hapiOrder = (Order<Bundle>) order;
        var orderBundle = hapiOrder.getUnderlyingOrder();

        var messageHeader =
                HapiHelper.resourcesInBundle(orderBundle, MessageHeader.class)
                        .findFirst()
                        .orElse(null);

        if (messageHeader == null) {
            messageHeader = new MessageHeader();
            orderBundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        }

        messageHeader.setEvent(OML_CODING);

        return new HapiOrder(orderBundle);
    }

    @Override
    public Order<?> addContactSectionToPatientResource(Order<?> order) {
        logger.logInfo("Adding contact section in Patient resource");

        var hapiOrder = (Order<Bundle>) order;
        var orderBundle = hapiOrder.getUnderlyingOrder();

        HapiHelper.resourcesInBundle(orderBundle, Patient.class)
                .forEach(
                        p -> {
                            var myContact = p.addContact();
                            var motherRelationship = myContact.addRelationship();
                            motherRelationship.setCoding(CODING_LIST);

                            var mothersMaidenNameExtension =
                                    p.getExtensionByUrl(
                                            "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName");
                            if (mothersMaidenNameExtension != null) {
                                myContact.setName(
                                        p.castToHumanName(mothersMaidenNameExtension.getValue()));
                            }
                            myContact.setTelecom(p.getTelecom());
                            myContact.setAddress(p.getAddressFirstRep());
                        });

        return new HapiOrder(orderBundle);
    }

    private MessageHeader createMessageHeader() {
        logger.logInfo("Creating new MessageHeader");

        var messageHeader = new MessageHeader();

        messageHeader.setId(UUID.randomUUID().toString());

        messageHeader.setEvent(OML_CODING);

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

    private Provenance createProvenanceResource(Date orderDate) {
        logger.logInfo("Creating new Provenance");
        var provenance = new Provenance();

        provenance.setId(UUID.randomUUID().toString());
        provenance.setRecorded(orderDate);
        provenance.setActivity(new CodeableConcept(OML_CODING));

        return provenance;
    }
}
