package gov.hhs.cdc.trustedintermediary.etor.operationoutcomes;

/**
 * Wrapper interface for our public facing metadata. Wraps an operation outcomes object to be
 * returned to our ReST API
 *
 * @param <T>
 */
public interface FhirMetadata<T> {

    T getUnderlyingOutcome();
}
