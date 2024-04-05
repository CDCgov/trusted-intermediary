package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;

/**
 * Interface to wrap a third-party demographics class (Ex: Hapi FHIR Bundle)
 *
 * @param <T> The underlying FHIR demographics type.
 */
public interface Demographics<T> extends FhirResource<T> {
    String getPatientId();
}
