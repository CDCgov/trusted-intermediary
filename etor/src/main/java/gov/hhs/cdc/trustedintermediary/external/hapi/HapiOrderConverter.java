package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
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
public class HapiOrderConverter {
    private static final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");

    private static final List<Coding> CODING_LIST =
            List.of(
                    new Coding(
                            "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH", "mother"));

    public static void convertDemographicsToOrder(Bundle demographics) {
        var overallId = UUID.randomUUID().toString();
        if (!demographics.hasId()) {
            demographics.setId(overallId);
        }

        if (!demographics.hasIdentifier()) {
            demographics.setIdentifier(new Identifier().setValue(overallId));
        }

        var orderDateTime = Date.from(Instant.now());
        if (!demographics.hasTimestamp()) {
            demographics.setTimestamp(orderDateTime);
        }

        demographics.setType(
                Bundle.BundleType.MESSAGE); // it always needs to be a message, so no if statement

        var patient =
                HapiHelper.resourcesInBundle(demographics, Patient.class).findFirst().orElse(null);

        var serviceRequest = createServiceRequest(patient, orderDateTime);
        var messageHeader = createOmlMessageHeader();
        var provenance = createProvenanceResource(orderDateTime);

        demographics
                .getEntry()
                .add(0, new Bundle.BundleEntryComponent().setResource(messageHeader));
        demographics.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest));
        demographics.addEntry(new Bundle.BundleEntryComponent().setResource(provenance));
    }

    public static void convertToOmlOrder(Bundle order) {
        var messageHeader = HapiMessageConverterHelper.findOrInitializeMessageHeader(order);
        messageHeader.setEvent(OML_CODING);
    }

    public static void addContactSectionToPatientResource(Bundle order) {
        HapiHelper.resourcesInBundle(order, Patient.class)
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
    }

    public static MessageHeader createOmlMessageHeader() {
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

    public static ServiceRequest createServiceRequest(
            final Patient patient, final Date orderDateTime) {
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

    public static Provenance createProvenanceResource(Date orderDate) {
        var provenance = new Provenance();

        provenance.setId(UUID.randomUUID().toString());
        provenance.setRecorded(orderDate);
        provenance.setActivity(new CodeableConcept(OML_CODING));

        return provenance;
    }

    public static Patient findPatientOrNull(Bundle bundle) {
        return HapiHelper.resourcesInBundle(bundle, Patient.class).findFirst().orElse(null);
    }

    public static Stream<Patient> findAllPatients(Bundle bundle) {
        return HapiHelper.resourcesInBundle(bundle, Patient.class);
    }
}
