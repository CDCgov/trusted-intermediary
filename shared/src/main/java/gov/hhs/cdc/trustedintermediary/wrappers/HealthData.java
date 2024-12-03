package gov.hhs.cdc.trustedintermediary.wrappers;

/**
 * Represents a generic health data object. The data object could be a HL7 message or FHIR resource
 */
public interface HealthData<T> {
    T getUnderlyingData();

    default String getIdentifier() {
        return "";
    }
}
