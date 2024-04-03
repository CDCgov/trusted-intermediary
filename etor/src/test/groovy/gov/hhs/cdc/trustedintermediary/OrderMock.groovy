package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.orders.Order

/**
 * A mock implementation of the {@link Order} interface that is easy to use in tests.
 */
class OrderMock<T> implements Order<T> {

    private String fhirResourceId
    private String patientId
    private T underlyingOrders
    private String placerOrderNumber
    private MessageHdDataType sendingApplicationDetails
    private MessageHdDataType sendingFacilityDetails
    private MessageHdDataType receivingApplicationDetails
    private MessageHdDataType receivingFacilityDetails

    OrderMock(String fhirResourceId, String patientId, T underlyingOrders, String placerOrderNumber, MessageHdDataType sendingApplicationDetails, MessageHdDataType sendingFacilityDetails,
    MessageHdDataType receivingApplicationDetails, MessageHdDataType receivingFacilityDetails) {
        this.fhirResourceId = fhirResourceId
        this.patientId = patientId
        this.underlyingOrders = underlyingOrders
        this.placerOrderNumber = placerOrderNumber
        this.sendingApplicationDetails = sendingApplicationDetails
        this.sendingFacilityDetails = sendingFacilityDetails
        this.receivingApplicationDetails = receivingApplicationDetails
        this.receivingFacilityDetails = receivingFacilityDetails
    }

    @Override
    T getUnderlyingOrder() {
        return this.underlyingOrders
    }

    @Override
    String getFhirResourceId() {
        return this.fhirResourceId
    }

    @Override
    String getPatientId() {
        return patientId
    }

    @Override
    String getPlacerOrderNumber() {
        return this.placerOrderNumber
    }

    @Override
    MessageHdDataType getSendingApplicationDetails() {
        return this.sendingApplicationDetails
    }

    @Override
    MessageHdDataType getSendingFacilityDetails() {
        return this.sendingFacilityDetails
    }

    @Override
    MessageHdDataType getReceivingApplicationDetails() {
        return this.receivingApplicationDetails
    }

    @Override
    MessageHdDataType getReceivingFacilityDetails() {
        return this.receivingFacilityDetails
    }
}
