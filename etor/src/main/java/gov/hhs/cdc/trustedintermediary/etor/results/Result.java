package gov.hhs.cdc.trustedintermediary.etor.results;

public interface Result<T> {
    T getUnderlyingResult();

    String getFhirResourceId();

    String getPatientId();
}
