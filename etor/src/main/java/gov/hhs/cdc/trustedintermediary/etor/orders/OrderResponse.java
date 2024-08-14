package gov.hhs.cdc.trustedintermediary.etor.orders;

/** Response for the v1/etor/orders endpoint. */
public final class OrderResponse {

    private final String fhirResourceId;
    private final String patientId;

    OrderResponse(String fhirResourceId, String patientId) {
        this.fhirResourceId = fhirResourceId;
        this.patientId = patientId;
    }

    public OrderResponse(Order<?> orders) {
        this(orders.getFhirResourceId(), orders.getPatientId());
    }

    public String getFhirResourceId() {
        return fhirResourceId;
    }

    public String getPatientId() {
        return patientId;
    }
}
