package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.messages.Message;

/** Interface to wrap a third-party lab result class (Ex: Hapi FHIR Bundle) */
public interface Result<T> extends Message<T> {}
