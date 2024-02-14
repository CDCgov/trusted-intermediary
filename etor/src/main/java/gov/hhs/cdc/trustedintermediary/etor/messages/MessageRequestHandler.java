package gov.hhs.cdc.trustedintermediary.etor.messages;

import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;

/**
 * Functional interface for handling message requests.
 *
 * @param <T> the type of the response
 */
@FunctionalInterface
public interface MessageRequestHandler<T> {
    /**
     * Parses the request, converts and sends the message
     *
     * @param receivedSubmissionId the ID for the submission returned from ReportStream
     * @return the response
     * @throws FhirParseException if there is an error parsing the FHIR data
     * @throws UnableToSendMessageException if there is an error sending the message
     */
    T handle(String receivedSubmissionId) throws FhirParseException, UnableToSendMessageException;
}
