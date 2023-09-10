package gov.hhs.cdc.trustedintermediary.etor.tasks;

public class TaskResponse {

    private String taskId;
    private String serviceRequestId;
    private String specimenId;

    public TaskResponse(Task<?> task) {
        setTaskId(task.getTaskId());
        setServiceRequestId(task.getServiceRequestId());
        setSpecimenId(task.getSpecimenId());
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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
