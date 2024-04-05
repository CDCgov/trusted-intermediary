package gov.hhs.cdc.trustedintermediary.etor.messages;

/**
 * Defines the structure and operations for a message. This interface allows for the retrieval of
 * various pieces of information related to a message, including details about the sending and
 * receiving applications and facilities, as well as order numbers.
 */
public interface Message<T> {
    T getUnderlyingElement();

    String getFhirResourceId();

    String getPlacerOrderNumber();

    MessageHdDataType getSendingApplicationDetails();

    MessageHdDataType getSendingFacilityDetails();

    MessageHdDataType getReceivingApplicationDetails();

    MessageHdDataType getReceivingFacilityDetails();
}
