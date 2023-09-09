package gov.hhs.cdc.trustedintermediary.etor.orders;

public class TaskResponse {

    private String fhirResourceId;
    private String serviceRequestId;
    private String specimenId;

    public TaskResponse(Task<?> task) {
        setFhirResourceId(task.getFhirResourceId());
        setServiceRequestId(task.getServiceRequestId());
        setSpecimenId(task.getSpecimenId());
    }

    public String getFhirResourceId() {
        return fhirResourceId;
    }

    public void setFhirResourceId(String fhirResourceId) {
        this.fhirResourceId = fhirResourceId;
    }

    public String getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(String serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }
}
