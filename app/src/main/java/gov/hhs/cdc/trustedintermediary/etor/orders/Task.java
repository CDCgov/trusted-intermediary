package gov.hhs.cdc.trustedintermediary.etor.orders;

public interface Task<T> {
    T getUnderlyingTask();

    String getTaskId();

    String getServiceRequestId();

    String getSpecimenId();
}
