package gov.hhs.cdc.trustedintermediary.etor.messages;

import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;

@FunctionalInterface
public interface MessageRequestHandler<T> {
    T handle(String receivedSubmissionId) throws FhirParseException, UnableToSendMessageException;
}
