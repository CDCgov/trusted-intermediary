package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.messages.Message;

/**
 * Interface to wrap a third-party lab result class (Ex: Hapi FHIR Bundle)
 *
 * @param <T> The underlying FHIR lab result type.
 */
public interface Result<T> extends Message<T> {}
