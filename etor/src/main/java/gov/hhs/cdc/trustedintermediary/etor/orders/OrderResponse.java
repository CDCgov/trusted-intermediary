package gov.hhs.cdc.trustedintermediary.etor.orders;

/** Response for the v1/etor/orders endpoint. */
public record OrderResponse(String fhirResourceId, String patientId) {

    public OrderResponse(Order<?> orders) {
        this(orders.getFhirResourceId(), orders.getPatientId());
    }
}
