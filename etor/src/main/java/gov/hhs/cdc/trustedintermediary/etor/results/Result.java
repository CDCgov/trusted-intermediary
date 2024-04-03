package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;

/**
 * Interface to wrap a third-party lab result class (Ex: Hapi FHIR Bundle)
 *
 * @param <T> The underlying FHIR lab result type.
 */
public interface Result<T> {
    T getUnderlyingResult();

    String getFhirResourceId();

    String getPlacerOrderNumber();

    MessageHdDataType getSendingApplicationDetails();

    MessageHdDataType getSendingFacilityDetails();

    MessageHdDataType getReceivingApplicationDetails();

    MessageHdDataType getReceivingFacilityDetails();
}
