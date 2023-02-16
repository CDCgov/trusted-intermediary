package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographics;
import java.util.Date;
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
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;

/**
 * Converts {@link PatientDemographics} to a Hapi-specific FHIR lab order ({@link HapiLabOrder} or
 * {@link LabOrder<Bundle>}).
 */
public class HapiLabOrderConverter implements LabOrderConverter {
    private static final HapiLabOrderConverter INSTANCE = new HapiLabOrderConverter();

    public static HapiLabOrderConverter getInstance() {
        return INSTANCE;
    }

    private HapiLabOrderConverter() {}

    @Override
    public LabOrder<Bundle> convertToOrder(final PatientDemographics demographics) {
        var labOrder = new Bundle();

        labOrder.addEntry(
                new Bundle.BundleEntryComponent().setResource(createPatientResource(demographics)));

        return new HapiLabOrder(labOrder);
    }

    private Resource createPatientResource(final PatientDemographics demographics) {
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
}
