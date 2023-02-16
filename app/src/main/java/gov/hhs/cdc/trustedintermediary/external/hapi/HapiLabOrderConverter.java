package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographics;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

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
        labOrder.setId(demographics.getFhirResourceId());
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

        // birth date
        //        patient.setBirthDateElement(new
        // DateType().addExtension("http://hl7.org/fhir/StructureDefinition/patient-birthTime", new
        // DateTimeType(demographics.getBirthDateTime().to)))

        patient.setMultipleBirth(new IntegerType(demographics.getBirthOrder()));

        // race
        //        patient.

        // next of kind

        return patient;
    }
}
