package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;

/**
 * The RSEndpointClient interface represents a client for interacting with or mocking ReportStream
 * API endpoints
 */
public interface RSEndpointClient {

    /**
     * Retrieves the Report Stream token.
     *
     * @return The Report Stream token.
     * @throws ReportStreamEndpointClientException If an error occurs while retrieving the token.
     */
    String getRsToken() throws ReportStreamEndpointClientException;

    /**
     * Sends a request to the Waters endpoint.
     *
     * @param body The request body.
     * @param bearerToken The bearer token for authentication.
     * @return The response from the Waters endpoint.
     * @throws ReportStreamEndpointClientException If an error occurs while sending the request.
     */
    String requestWatersEndpoint(String body, String bearerToken)
            throws ReportStreamEndpointClientException;

    /**
     * Sends a request to the History endpoint.
     *
     * @param submissionId The ID of the submission.
     * @param bearerToken The bearer token for authentication.
     * @return The response from the History endpoint.
     * @throws ReportStreamEndpointClientException If an error occurs while sending the request.
     */
    String requestHistoryEndpoint(String submissionId, String bearerToken)
            throws ReportStreamEndpointClientException;
}
