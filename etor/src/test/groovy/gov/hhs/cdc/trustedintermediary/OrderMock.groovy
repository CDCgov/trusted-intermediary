package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.orders.Order

/**
 * A mock implementation of the {@link Order} interface that is easy to use in tests.
 */
class OrderMock<T> implements Order<T> {

    private String fhirResourceId
    private String patientId
    private T underlyingOrders

    OrderMock(String fhirResourceId, String patientId, T underlyingOrders) {
        this.fhirResourceId = fhirResourceId
        this.patientId = patientId
        this.underlyingOrders = underlyingOrders
    }

    @Override
    T getUnderlyingOrder() {
        return underlyingOrders
    }

    @Override
    String getFhirResourceId() {
        return fhirResourceId
    }

    @Override
    String getPatientId() {
        return patientId
    }

    @Override
    String getPlacerOrderNumber() {
        return null
    }

    @Override
    String getSendingApplicationId() {
        return null
    }

    @Override
    String getSendingFacilityId() {
        return null
    }

    @Override
    String getReceivingApplicationId() {
        return null
    }

    @Override
    String getReceivingFacilityId() {
        return null
    }
}
