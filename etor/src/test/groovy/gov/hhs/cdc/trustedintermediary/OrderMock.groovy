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
    private String sendingApplicationId
    private String sendingFacilityId
    private String receivingApplicationId
    private String receivingFacilityId

    OrderMock(String fhirResourceId, String patientId, T underlyingOrders, String placerOrderNumber, String sendingApplicationId, String sendingFacilityId,
    String receivingApplicationId, String receivingFacilityId) {
        this.fhirResourceId = fhirResourceId
        this.patientId = patientId
        this.underlyingOrders = underlyingOrders
        this.placerOrderNumber = placerOrderNumber
        this.sendingApplicationId = sendingApplicationId
        this.sendingFacilityId = sendingFacilityId
        this.receivingApplicationId = receivingApplicationId
        this.receivingFacilityId = receivingFacilityId
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
    String getSendingApplicationId() {
        return this.sendingApplicationId
    }

    @Override
    String getSendingFacilityId() {
        return this.sendingFacilityId
    }

    @Override
    String getReceivingApplicationDetails() {
        return this.receivingApplicationId
    }

    @Override
    String getReceivingFacilityId() {
        return this.receivingFacilityId
    }
}
