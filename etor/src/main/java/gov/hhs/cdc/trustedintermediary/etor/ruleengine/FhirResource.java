package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

/**
 * This interface represents a FHIR resource. It's used as a wrapper to decouple dependency on third
 * party libraries.
 *
 * @param <T> the type of the underlying resource
 */
public interface FhirResource<T> {
    T getUnderlyingResource();

    String getFhirResourceId();
}
