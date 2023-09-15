package gov.hhs.cdc.trustedintermediary.etor.demographics;

/**
 * Interface to wrap a third-party demographics class (Ex: Hapi FHIR Bundle)
 *
 * @param <T> The underlying FHIR demographics type.
 */
public interface Demographics<T> {
    T getUnderlyingDemographics();

    String getFhirResourceId();

    String getPatientId();
}
