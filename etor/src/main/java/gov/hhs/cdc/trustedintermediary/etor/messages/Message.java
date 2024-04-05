package gov.hhs.cdc.trustedintermediary.etor.messages;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;

/**
 * Defines the structure and operations for a message. This interface allows for the retrieval of
 * various pieces of information related to a message, including details about the sending and
 * receiving applications and facilities, as well as order numbers.
 */
public interface Message<T> extends FhirResource<T> {
    String getPlacerOrderNumber();

    MessageHdDataType getSendingApplicationDetails();

    MessageHdDataType getSendingFacilityDetails();

    MessageHdDataType getReceivingApplicationDetails();

    MessageHdDataType getReceivingFacilityDetails();
}
