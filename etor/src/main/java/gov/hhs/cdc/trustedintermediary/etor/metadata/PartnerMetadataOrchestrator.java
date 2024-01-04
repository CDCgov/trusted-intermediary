package gov.hhs.cdc.trustedintermediary.etor.metadata;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.time.Instant;
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
    @Inject RSEndpointClient rsclient;
    @Inject Formatter formatter;
    @Inject Logger logger;

    public static PartnerMetadataOrchestrator getInstance() {
        return INSTANCE;
    }

    private PartnerMetadataOrchestrator() {}

    public void updateMetadataForReceivedOrder(String receivedSubmissionId, Order<?> order)
            throws PartnerMetadataException {
        // currently blocked by: https://github.com/CDCgov/prime-reportstream/issues/12624
        // once we get the right receivedSubmissionId from RS, this method should work
        logger.logInfo(
                "Looking up sender name and timeReceived from RS history API for receivedSubmissionId: {}",
                receivedSubmissionId);

        String sender;
        Instant timeReceived;
        String hash;
        try {
            String bearerToken = rsclient.getRsToken();
            String responseBody =
                    rsclient.requestHistoryEndpoint(receivedSubmissionId, bearerToken);
            Map<String, Object> responseObject =
                    formatter.convertJsonToObject(responseBody, new TypeReference<>() {});

            sender = responseObject.get("sender").toString();
            String timestamp = responseObject.get("timestamp").toString();
            timeReceived = Instant.parse(timestamp);
            hash = String.valueOf(order.hashCode());

        } catch (Exception e) {
            throw new PartnerMetadataException(
                    "Unable to retrieve metadata from RS history API", e);
        }

        logger.logInfo(
                "Updating metadata with sender: {}, timeReceived: {} and hash",
                sender,
                timeReceived);
        PartnerMetadata partnerMetadata =
                new PartnerMetadata(receivedSubmissionId, sender, timeReceived, hash);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public void updateMetadataForSentOrder(String receivedSubmissionId, String sentSubmissionId)
            throws PartnerMetadataException {

        if (sentSubmissionId == null) {
            return;
        }

        PartnerMetadata partnerMetadata =
                partnerMetadataStorage
                        .readMetadata(receivedSubmissionId)
                        .orElseThrow(
                                () ->
                                        new PartnerMetadataException(
                                                "Couldn't find metadata for receivedSubmissionId: "
                                                        + receivedSubmissionId));
        if (!sentSubmissionId.equals(partnerMetadata.sentSubmissionId())) {
            logger.logInfo("Updating metadata with sentSubmissionId: {}", sentSubmissionId);
            partnerMetadata = partnerMetadata.withSentSubmissionId(sentSubmissionId);
            partnerMetadataStorage.saveMetadata(partnerMetadata);
        }

        logger.logInfo(
                "Looking up receiver name from RS history API for sentSubmissionId: {}",
                sentSubmissionId);
        String receiver;
        try {
            String bearerToken = rsclient.getRsToken();
            String responseBody = rsclient.requestHistoryEndpoint(sentSubmissionId, bearerToken);
            receiver = getReceiverName(responseBody);
        } catch (ReportStreamEndpointClientException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Unable to retrieve metadata from RS history API", e);
        }

        logger.logInfo("Updating metadata with receiver: {}", receiver);
        partnerMetadata = partnerMetadata.withReceiver(receiver);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public Optional<PartnerMetadata> getMetadata(String receivedSubmissionId)
            throws PartnerMetadataException {
        PartnerMetadata partnerMetadata =
                partnerMetadataStorage
                        .readMetadata(receivedSubmissionId)
                        .orElseThrow(
                                () ->
                                        new PartnerMetadataException(
                                                "Couldn't find metadata for receivedSubmissionId: "
                                                        + receivedSubmissionId));

        if (partnerMetadata.receiver() == null && partnerMetadata.sentSubmissionId() != null) {
            logger.logInfo("Receiver name not found in metadata, looking up from RS history API");
            updateMetadataForSentOrder(receivedSubmissionId, partnerMetadata.sentSubmissionId());
            return partnerMetadataStorage.readMetadata(receivedSubmissionId);
        }

        return Optional.of(partnerMetadata);
    }

    String getReceiverName(String responseBody) throws FormatterProcessingException {
        // the expected json structure for the response is:
        // {
        //    ...
        //    "destinations" : [ {
        //        ...
        //        "organization_id" : "flexion",
        //        "service" : "simulated-lab",
        //        ...
        //    } ],
        //    ...
        // }

        String organizationId;
        String service;
        try {
            Map<String, Object> responseObject =
                    formatter.convertJsonToObject(responseBody, new TypeReference<>() {});
            ArrayList<?> destinations = (ArrayList<?>) responseObject.get("destinations");
            Map<?, ?> destination = (Map<?, ?>) destinations.get(0);
            organizationId = destination.get("organization_id").toString();
            service = destination.get("service").toString();
        } catch (Exception e) {
            throw new FormatterProcessingException(
                    "Unable to extract receiver name from response due to unexpected format", e);
        }

        return organizationId + "." + service;
    }
}
