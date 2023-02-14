package gov.hhs.cdc.trustedintermediary.etor.demographics;

public interface LabOrderConverter {
    LabOrder<?> convertToOrder(PatientDemographics demographics);
}
