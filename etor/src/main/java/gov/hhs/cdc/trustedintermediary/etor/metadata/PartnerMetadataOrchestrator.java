package gov.hhs.cdc.trustedintermediary.etor.metadata;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
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
    @Inject ReportStreamEndpointClient rsclient;
    @Inject Formatter formatter;

    public static PartnerMetadataOrchestrator getInstance() {
        return INSTANCE;
    }

    private PartnerMetadataOrchestrator() {}

    public void updateMetadataForReceivedOrder(String receivedSubmissionId, Order<?> order)
            throws PartnerMetadataException {
        // currently blocked by: https://github.com/CDCgov/prime-reportstream/issues/12624
        // once we get the right receivedSubmissionId from RS, this method should work

        Map<String, Object> responseObject;
        try {
            String bearerToken = rsclient.getRsToken();
            String responseBody =
                    rsclient.requestHistoryEndpoint(receivedSubmissionId, bearerToken);
            responseObject = formatter.convertJsonToObject(responseBody, new TypeReference<>() {});

        } catch (ReportStreamEndpointClientException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Unable to retrieve metadata from RS history API", e);
        }

        String sender = responseObject.get("sender").toString();
        Instant timeReceived = Instant.parse(responseObject.get("timestamp").toString());
        String hash = String.valueOf(order.hashCode());
        PartnerMetadata partnerMetadata =
                new PartnerMetadata(receivedSubmissionId, sender, timeReceived, hash);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public void updateMetadataForSentOrder(String receivedSubmissionId, String sentSubmissionId)
            throws PartnerMetadataException {

        PartnerMetadata partnerMetadata =
                partnerMetadataStorage.readMetadata(receivedSubmissionId).orElseThrow();
        if (!Objects.equals(partnerMetadata.sentSubmissionId(), sentSubmissionId)) {
            partnerMetadata = partnerMetadata.withSentSubmissionId(sentSubmissionId);
            partnerMetadataStorage.saveMetadata(partnerMetadata);
        }

        String receiver;
        try {
            String bearerToken = rsclient.getRsToken();
            String responseBody = rsclient.requestHistoryEndpoint(sentSubmissionId, bearerToken);
            receiver = getReceiverName(responseBody);
        } catch (ReportStreamEndpointClientException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Unable to retrieve metadata from RS history API", e);
        }

        partnerMetadata = partnerMetadata.withReceiver(receiver);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public Optional<PartnerMetadata> getMetadata(String receivedSubmissionId)
            throws PartnerMetadataException {
        PartnerMetadata partnerMetadata =
                partnerMetadataStorage.readMetadata(receivedSubmissionId).orElseThrow();

        if (partnerMetadata.receiver() == null && partnerMetadata.sentSubmissionId() != null) {
            updateMetadataForSentOrder(receivedSubmissionId, partnerMetadata.sentSubmissionId());
            return partnerMetadataStorage.readMetadata(receivedSubmissionId);
        }

        return Optional.of(partnerMetadata);
    }

    String getReceiverName(String responseBody) throws FormatterProcessingException {
        Map<String, Object> responseObject =
                formatter.convertJsonToObject(responseBody, new TypeReference<>() {});
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
