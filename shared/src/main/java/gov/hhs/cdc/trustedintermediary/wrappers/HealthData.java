package gov.hhs.cdc.trustedintermediary.wrappers;

public interface HealthData<T> {
    T getUnderlyingData();

    default String getName() {
        return "";
    }
}
