package gov.hhs.cdc.trustedintermediary.etor.orders;

/** Response for the v1/etor/orders endpoint. */
public class OrderResponse {

    private String fhirResourceId;
    private String patientId;

    OrderResponse(String fhirResourceId, String patientId) {
        setFhirResourceId(fhirResourceId);
        setPatientId(patientId);
    }

    public OrderResponse(Order<?> orders) {
        setFhirResourceId(orders.getFhirResourceId());
        setPatientId(orders.getPatientId());
    }

    public String getFhirResourceId() {
        return fhirResourceId;
    }

    public void setFhirResourceId(String fhirResourceId) {
        this.fhirResourceId = fhirResourceId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
