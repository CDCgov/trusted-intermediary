package gov.hhs.cdc.trustedintermediary.etor.results;

/**
 * Interface to wrap a third-party lab result class (Ex: Hapi FHIR Bundle)
 *
 * @param <T> The underlying FHIR lab result type.
 */
public interface Result<T> {
    T getUnderlyingResult();

    String getFhirResourceId();

    String getPlacerOrderNumber();

    String getSendingApplicationId();

    String getSendingFacilityId();

    String getReceivingApplicationId();

    String getReceivingFacilityId();
}
