package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;

/** Response for the v1/etor/orders endpoint. */
public class OrdersResponse {

    private String fhirResourceId;
    private String patientId;

    OrdersResponse(String fhirResourceId, String patientId) {
        setFhirResourceId(fhirResourceId);
        setPatientId(patientId);
    }

    public OrdersResponse(LabOrder<?> orders) {
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
