package gov.hhs.cdc.trustedintermediary.etor.metadata;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

/**
 * The PartnerMetadataOrchestrator class is responsible for updating and retrieving partner-facing
 * metadata. It interacts with the metadata storage and the history API to create, update, and store
 * metadata.
 */
public class PartnerMetadataOrchestrator {

    private static final PartnerMetadataOrchestrator INSTANCE = new PartnerMetadataOrchestrator();

    @Inject PartnerMetadataStorage partnerMetadataStorage;
    @Inject private ReportStreamEndpointClient rsclient;
    @Inject private Formatter formatter;

    public static PartnerMetadataOrchestrator getInstance() {
        return INSTANCE;
    }

    private PartnerMetadataOrchestrator() {}

    public void updateMetadataForReceivedOrder(String submissionId, Order<?> order)
            throws PartnerMetadataException {
        // will call the RS history API given the submissionId (albeit, right now this won't work
        // given the way RS works).
        // from the history API response, extract the sender (organization + sender client), and
        // time received.
        // we will calculate the hash.
        // then we call the metadata storage to save this stuff.

        PartnerMetadata partnerMetadata = new PartnerMetadata(submissionId);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public void updateMetadataForSentOrder(
            String receivedSubmissionId, String sentSubmissionId, Order<?> order)
            throws PartnerMetadataException {

        String receiver;
        try {
            String bearerToken = rsclient.getRsToken();
            String responseBody = rsclient.requestHistoryEndpoint(sentSubmissionId, bearerToken);
            receiver = getReceiverName(responseBody);
        } catch (ReportStreamEndpointClientException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Unable to retrieve metadata from RS history API", e);
        }

        PartnerMetadata partnerMetadata =
                new PartnerMetadata(
                        receivedSubmissionId, sentSubmissionId, null, receiver, null, null);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public Optional<PartnerMetadata> getMetadata(String submissionId)
            throws PartnerMetadataException {
        // call the metadata storage to get the metadata.
        // check if the receiver is filled out, and if it isn't, call the RS history API to get the
        // receiver.
        // if had to call the history API, extract the receiver and call the metadata storage to
        // save the metadata with the receiver added.
        // return the metadata.
        return partnerMetadataStorage.readMetadata(submissionId);
    }

    private String getReceiverName(String clientResponse) throws FormatterProcessingException {
        Map<String, Object> responseObject =
                formatter.convertJsonToObject(clientResponse, new TypeReference<>() {});
        Object destinationsObj = responseObject.get("destinations");
        if (!(destinationsObj instanceof ArrayList<?> destinationsList)) {
            throw new FormatterProcessingException(
                    "destinations is not an ArrayList", new Exception());
        }

        if (destinationsList.isEmpty()
                || !(destinationsList.get(0) instanceof Map<?, ?> destination)) {
            throw new FormatterProcessingException(
                    "First item in destinations is not a Map", new Exception());
        }

        var organizationId = destination.get("organization_id");
        var service = destination.get("service");

        if (organizationId == null || service == null) {
            throw new FormatterProcessingException(
                    "organization_id or service is null", new Exception());
        }

        return organizationId + "." + service;
    }
}
