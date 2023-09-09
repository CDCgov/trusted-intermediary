package gov.hhs.cdc.trustedintermediary.etor.orders;

public interface Task<T> {
    T getUnderlyingTask();

    String getFhirResourceId();

    String getServiceRequestId();

    String getSpecimenId();
}
