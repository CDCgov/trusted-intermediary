package gov.hhs.cdc.trustedintermediary.wrappers;

public interface HealthData<T> {
    T getUnderlyingData();

    String getName();
}
