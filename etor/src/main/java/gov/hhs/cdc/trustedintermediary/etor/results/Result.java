package gov.hhs.cdc.trustedintermediary.etor.results;

/**
 * Filler Result Interface for now with Generic Type Result
 *
 * @param <T>
 */
public interface Result<T> {
    T getUnderlyingResult();

    String getFhirResourceId();

    String getPatientId();
}
