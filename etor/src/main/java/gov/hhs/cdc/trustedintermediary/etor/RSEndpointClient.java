package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;

import javax.annotation.Nonnull;

public interface RSEndpointClient {

    String requestWatersEndpoint(@Nonnull String body, @Nonnull String bearerToken)
            throws ReportStreamEndpointClientException;

    String requestHistoryEndpoint(@Nonnull String submissionId, @Nonnull String bearerToken)
            throws ReportStreamEndpointClientException;

    String getRsToken() throws ReportStreamEndpointClientException;
}
