package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.orders.Order

/**
 * A mock implementation of the {@link Order} interface that is easy to use in tests.
 */
class OrderMock<T> implements Order<T> {

    private String fhirResourceId
    private String patientId
    private T underlyingOrders
    private String placerOrderNumber
    private String sendingApplicationDetails
    private String sendingFacilityDetails
    private String receivingApplicationDetails
    private String receivingFacilityDetails

    OrderMock(String fhirResourceId, String patientId, T underlyingOrders, String placerOrderNumber, String sendingApplicationId, String sendingFacilityId,
    String receivingApplicationDetails, String receivingFacilityDetails) {
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
    String getSendingApplicationDetails() {
        return this.sendingApplicationDetails
    }

    @Override
    String getSendingFacilityDetails() {
        return this.sendingFacilityDetails
    }

    @Override
    String getReceivingApplicationDetails() {
        return this.receivingApplicationDetails
    }

    @Override
    String getReceivingFacilityDetails() {
        return this.receivingFacilityDetails
    }
}
