package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographics;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UrlType;

/**
 * Converts {@link PatientDemographics} to a Hapi-specific FHIR lab order ({@link HapiLabOrder} or
 * {@link LabOrder<Bundle>}).
 */
public class HapiLabOrderConverter implements LabOrderConverter {
    private static final HapiLabOrderConverter INSTANCE = new HapiLabOrderConverter();

    @Inject Logger logger;

    public static HapiLabOrderConverter getInstance() {
        return INSTANCE;
    }

    private HapiLabOrderConverter() {}

    @Override
    public LabOrder<Bundle> convertToOrder(final PatientDemographics demographics) {
        logger.logInfo("Converting demographics to order");
        var orderDateTime = Date.from(Instant.now());
        var labOrder = new Bundle();
        var labOrderId = UUID.randomUUID().toString();
        var omlLabOrderCoding =
                new Coding(
                        "http://terminology.hl7.org/CodeSystem/v2-0003",
                        "O21",
                        "OML - Laboratory order");
        labOrder.setId(labOrderId);
        labOrder.setIdentifier(new Identifier().setValue(labOrderId));
        labOrder.setType(Bundle.BundleType.MESSAGE);
        labOrder.setTimestamp(orderDateTime);

        var patient = createPatientResource(demographics);
        var serviceRequest = createServiceRequest(patient, orderDateTime);
        var messageHeader = createMessageHeader(omlLabOrderCoding);
        var provenance = createProvenanceResource(orderDateTime, omlLabOrderCoding);

        labOrder.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        labOrder.addEntry(new Bundle.BundleEntryComponent().setResource(patient));
        labOrder.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest));
        labOrder.addEntry(new Bundle.BundleEntryComponent().setResource(provenance));

        return new HapiLabOrder(labOrder);
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

    private Patient createPatientResource(final PatientDemographics demographics) {
        logger.logInfo("Creating new Patient");

        var patient = new Patient();

        patient.setId(demographics.getFhirResourceId());
        patient.addIdentifier(
                new Identifier()
                        .setType(
                                new CodeableConcept(
                                        new Coding(
                                                "http://terminology.hl7.org/CodeSystem/v2-0203",
                                                "MR",
                                                "Medical Record Number")))
                        .setValue(demographics.getPatientId()));

        patient.addName(
                new HumanName()
                        .setFamily(demographics.getLastName())
                        .addGiven(demographics.getFirstName())
                        .setUse(HumanName.NameUse.OFFICIAL));

        patient.setGender(Enumerations.AdministrativeGender.fromCode(demographics.getSex()));

        // birth date and time
        var birthDate = new DateType(Date.from(demographics.getBirthDateTime().toInstant()));
        birthDate.addExtension(
                "http://hl7.org/fhir/StructureDefinition/patient-birthTime",
                new DateTimeType(Date.from(demographics.getBirthDateTime().toInstant())));
        patient.setBirthDateElement(birthDate);

        patient.setMultipleBirth(new IntegerType(demographics.getBirthOrder()));

        // race
        var raceExtension =
                new Extension()
                        .setUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race");
        raceExtension.addExtension("text", new StringType(demographics.getRace()));
        patient.addExtension(raceExtension);

        // next of kin
        var nextOfKinRelationship =
                new CodeableConcept(
                        new Coding(
                                "http://terminology.hl7.org/CodeSystem/v2-0131",
                                "N",
                                "Next of kin"));
        var nextOfKinName =
                new HumanName()
                        .setFamily(demographics.getNextOfKin().getLastName())
                        .addGiven(demographics.getNextOfKin().getFirstName());
        var nextOfKinTelecom =
                new ContactPoint()
                        .setSystem(ContactPoint.ContactPointSystem.PHONE)
                        .setValue(demographics.getNextOfKin().getPhoneNumber());
        patient.addContact(
                new Patient.ContactComponent()
                        .addRelationship(nextOfKinRelationship)
                        .setName(nextOfKinName)
                        .addTelecom(nextOfKinTelecom));

        return patient;
    }

    private ServiceRequest createServiceRequest(final Patient patient, Date orderDate) {
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

        serviceRequest.setAuthoredOn(orderDate);

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
