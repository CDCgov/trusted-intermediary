package gov.hhs.cdc.trustedintermediary.etor.demographics;

/** Interface for converting a demographics object into a lab order. */
public interface LabOrderConverter {
    LabOrder<?> convertToOrder(PatientDemographics demographics);
}
