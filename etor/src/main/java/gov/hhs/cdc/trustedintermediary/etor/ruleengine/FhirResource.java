package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

public interface FhirResource<T> {
    T getUnderlyingResource();
}
