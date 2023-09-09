package gov.hhs.cdc.trustedintermediary.etor.tasks;

public interface Task<T> {
    T getUnderlyingTask();

    String getTaskId();

    String getServiceRequestId();

    String getSpecimenId();
}
