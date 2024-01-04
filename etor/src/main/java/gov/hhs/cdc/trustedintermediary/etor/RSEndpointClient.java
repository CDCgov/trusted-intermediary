package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;

/**
 * The RSEndpointClient interface represents a client for interacting with or mocking ReportStream
 * API endpoints
 */
public interface RSEndpointClient {

    String getRsToken() throws ReportStreamEndpointClientException;

    String requestWatersEndpoint(String body, String bearerToken)
            throws ReportStreamEndpointClientException;

    String requestHistoryEndpoint(String submissionId, String bearerToken)
            throws ReportStreamEndpointClientException;
}
