package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrder
/**
 * A mock implementation of the {@link LabOrder} interface that is easy to use in tests.
 */
class LabOrdersMock<T> implements LabOrder<T> {

    private String fhirResourceId
    private String patientId
    private T underlyingOrders

    LabOrdersMock(String fhirResourceId, String patientId, T underlyingOrders) {
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
}
