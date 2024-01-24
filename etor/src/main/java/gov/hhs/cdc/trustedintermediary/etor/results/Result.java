package gov.hhs.cdc.trustedintermediary.etor.results;

public interface Result<T> {
    T getUnderlyingOrder();

    String getFhirResourceId();

    String getPatientId();
}
