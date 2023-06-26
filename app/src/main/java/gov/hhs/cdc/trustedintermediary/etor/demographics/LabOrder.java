package gov.hhs.cdc.trustedintermediary.etor.demographics;

/**
 * Interface to wrap a third-party lab order class (Ex: Hapi FHIR Bundle)
 *
 * @param <T> The underlying FHIR lab order type.
 */
public interface LabOrder<T> {
    T getUnderlyingOrder();

    String getFhirResourceId();

    String getPatientId();
}
