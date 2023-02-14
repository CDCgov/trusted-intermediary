package gov.hhs.cdc.trustedintermediary.etor.demographics;

public interface LabOrder<T> {
    T getUnderlyingOrder();
}
