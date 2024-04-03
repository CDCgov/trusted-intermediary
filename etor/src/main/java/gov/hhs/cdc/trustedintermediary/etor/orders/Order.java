package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;

/**
 * Interface to wrap a third-party lab order class (Ex: Hapi FHIR Bundle)
 *
 * @param <T> The underlying FHIR lab order type.
 */
public interface Order<T> {
    T getUnderlyingOrder();

    String getFhirResourceId();

    String getPatientId();

    String getPlacerOrderNumber();

    MessageHdDataType getSendingApplicationDetails();

    MessageHdDataType getSendingFacilityDetails();

    MessageHdDataType getReceivingApplicationDetails();

    MessageHdDataType getReceivingFacilityDetails();
}
