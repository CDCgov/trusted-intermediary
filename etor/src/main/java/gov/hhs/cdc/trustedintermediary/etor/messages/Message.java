package gov.hhs.cdc.trustedintermediary.etor.messages;

public interface Message<T> {
    T getUnderlyingElement();

    String getFhirResourceId();

    String getPlacerOrderNumber();

    MessageHdDataType getSendingApplicationDetails();

    MessageHdDataType getSendingFacilityDetails();

    MessageHdDataType getReceivingApplicationDetails();

    MessageHdDataType getReceivingFacilityDetails();
}
