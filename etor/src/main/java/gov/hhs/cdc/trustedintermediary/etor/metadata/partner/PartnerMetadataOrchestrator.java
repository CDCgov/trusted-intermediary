package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * The PartnerMetadataOrchestrator class is responsible for updating and retrieving partner-facing
 * metadata. It interacts with the metadata storage and the history API to create, update, and store
 * metadata.
 */
public class PartnerMetadataOrchestrator {

    private static final PartnerMetadataOrchestrator INSTANCE = new PartnerMetadataOrchestrator();

    @Inject PartnerMetadataStorage partnerMetadataStorage;
    @Inject MessageLinkStorage messageLinkStorage;
    @Inject RSEndpointClient rsclient;
    @Inject Formatter formatter;
    @Inject Logger logger;

    public static PartnerMetadataOrchestrator getInstance() {
        return INSTANCE;
    }

    private PartnerMetadataOrchestrator() {}

    public void updateMetadataForReceivedMessage(PartnerMetadata partnerMetadata)
            throws PartnerMetadataException {

        logger.logInfo(
                "Looking up sender name and timeReceived from RS delivery API for receivedSubmissionId: {}",
                partnerMetadata.receivedSubmissionId());

        Instant timeReceived;
        try {
            String bearerToken = rsclient.getRsToken();
            String responseBody =
                    rsclient.requestDeliveryEndpoint(
                            partnerMetadata.receivedSubmissionId(), bearerToken);
            Map<String, Object> responseObject =
                    formatter.convertJsonToObject(responseBody, new TypeReference<>() {});

            List<Map<String, String>> originalIngestion =
                    (List<Map<String, String>>) responseObject.get("originalIngestion");

            if (originalIngestion == null || originalIngestion.isEmpty()) {
                throw new PartnerMetadataException(
                        "Ingestion time not found from RS delivery API response");
            }

            if (originalIngestion.size() > 1) {
                logger.logWarning(
                        "More than 1 report ids found in originalIngestion,"
                                + " check to make sure batching wasn't turned on for receiver in RS");
            }

            // We should only have 1 object in originalIngestion, it is a list to support other RS
            // use cases
            String timestamp = originalIngestion.get(0).get("ingestionTime");
            timeReceived = Instant.parse(timestamp);

        } catch (Exception e) {
            // write the received submission ID so that the rest of the metadata flow works even if
            // some data is missing
            logger.logWarning(
                    "Unable to retrieve metadata from RS delivery API, but writing basic metadata entry anyway for received submission ID {}",
                    partnerMetadata.receivedSubmissionId());
            partnerMetadataStorage.saveMetadata(partnerMetadata);

            throw new PartnerMetadataException(
                    "Unable to retrieve metadata from RS delivery API", e);
        }

        String sender = "PLACE_HOLDER";
        logger.logInfo("Updating metadata with sender: {}, timeReceived: {}", sender, timeReceived);
        PartnerMetadata updatedPartnerMetadata = partnerMetadata.withTimeReceived(timeReceived);
        partnerMetadataStorage.saveMetadata(updatedPartnerMetadata);
    }

    public void updateMetadataForSentMessage(String receivedSubmissionId, String sentSubmissionId)
            throws PartnerMetadataException {

        if (sentSubmissionId == null) {
            return;
        }

        Optional<PartnerMetadata> optionalPartnerMetadata =
                partnerMetadataStorage.readMetadata(receivedSubmissionId);
        if (optionalPartnerMetadata.isEmpty()) {
            logger.logWarning(
                    "Metadata not found for receivedSubmissionId: {}", receivedSubmissionId);
            return;
        }

        PartnerMetadata partnerMetadata = optionalPartnerMetadata.get();

        if (sentSubmissionId.equals(partnerMetadata.sentSubmissionId())) {
            return;
        }

        logger.logInfo("Updating metadata with sentSubmissionId: {}", sentSubmissionId);
        partnerMetadata = partnerMetadata.withSentSubmissionId(sentSubmissionId);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public Optional<PartnerMetadata> getMetadata(String receivedSubmissionId)
            throws PartnerMetadataException {
        Optional<PartnerMetadata> optionalPartnerMetadata =
                partnerMetadataStorage.readMetadata(receivedSubmissionId);
        if (optionalPartnerMetadata.isEmpty()) {
            logger.logInfo("Metadata not found for receivedSubmissionId: {}", receivedSubmissionId);
            return Optional.empty();
        }

        PartnerMetadata partnerMetadata = optionalPartnerMetadata.get();
        var sentSubmissionId = partnerMetadata.sentSubmissionId();
        if (metadataIsStale(partnerMetadata) && sentSubmissionId != null) {
            logger.logInfo(
                    "Receiver name not found in metadata or delivery status still pending, looking up {} from RS history API",
                    sentSubmissionId);

            String receiver;
            String rsStatus;
            String rsMessage = "";
            String timeDelivered;
            try {
                String bearerToken = rsclient.getRsToken();
                String responseBody =
                        rsclient.requestHistoryEndpoint(sentSubmissionId, bearerToken);
                var parsedResponseBody = getDataFromReportStream(responseBody);
                receiver = parsedResponseBody[0];
                rsStatus = parsedResponseBody[1];
                rsMessage = parsedResponseBody[2];
                timeDelivered = parsedResponseBody[3];
            } catch (ReportStreamEndpointClientException | FormatterProcessingException e) {
                throw new PartnerMetadataException(
                        "Unable to retrieve metadata from RS history API", e);
            }

            var ourStatus = ourStatusFromReportStreamStatus(rsStatus);

            logger.logInfo("Updating metadata with receiver {} and status {}", receiver, ourStatus);

            if (ourStatus == PartnerMetadataStatus.FAILED) {
                partnerMetadata = partnerMetadata.withFailureMessage(rsMessage);
            } else if (ourStatus == PartnerMetadataStatus.DELIVERED && timeDelivered != null) {
                partnerMetadata = partnerMetadata.withTimeDelivered(Instant.parse(timeDelivered));
            }

            partnerMetadataStorage.saveMetadata(partnerMetadata);
        }

        return Optional.of(partnerMetadata);
    }

    public void setMetadataStatusToFailed(String submissionId, String errorMessage)
            throws PartnerMetadataException {
        if (submissionId == null) {
            return;
        }

        Optional<PartnerMetadata> optionalPartnerMetadata =
                partnerMetadataStorage.readMetadata(submissionId);
        PartnerMetadata partnerMetadata;
        if (optionalPartnerMetadata.isEmpty()) {
            // there wasn't any metadata given the submission ID, so make one with the status
            partnerMetadata = new PartnerMetadata(submissionId, PartnerMetadataStatus.FAILED);
        } else {
            partnerMetadata = optionalPartnerMetadata.get();
            if (partnerMetadata.deliveryStatus().equals(PartnerMetadataStatus.FAILED)) {
                return;
            }
        }

        logger.logInfo(
                "Updating metadata delivery status {} with submissionId: {}",
                PartnerMetadataStatus.FAILED,
                submissionId);
        partnerMetadata =
                partnerMetadata
                        .withDeliveryStatus(PartnerMetadataStatus.FAILED)
                        .withFailureMessage(errorMessage);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public Map<String, Map<String, Object>> getConsolidatedMetadata(String senderName)
            throws PartnerMetadataException {

        var metadataSet = partnerMetadataStorage.readMetadataForSender(senderName);

        return metadataSet.stream()
                .collect(
                        Collectors.toMap(
                                PartnerMetadata::receivedSubmissionId,
                                metadata -> {
                                    var status = String.valueOf(metadata.deliveryStatus());
                                    var stale = metadataIsStale(metadata);
                                    var failureReason = metadata.failureReason();

                                    Map<String, Object> innerMap = new HashMap<>();
                                    innerMap.put("status", status);
                                    innerMap.put("stale", stale);
                                    innerMap.put("failureReason", failureReason);

                                    return innerMap;
                                }));
    }

    public Set<String> findMessagesIdsToLink(String receivedSubmissionId)
            throws PartnerMetadataException {
        var metadataSet =
                partnerMetadataStorage.readMetadataForMessageLinking(receivedSubmissionId);
        return metadataSet.stream()
                .map(PartnerMetadata::receivedSubmissionId)
                .collect(Collectors.toSet());
    }

    public void linkMessages(Set<String> messageIds) throws MessageLinkException {
        Optional<MessageLink> existingMessageLink = Optional.empty();
        for (String messageId : messageIds) {
            existingMessageLink = messageLinkStorage.getMessageLink(messageId);
            if (existingMessageLink.isPresent()) {
                break;
            }
        }

        if (existingMessageLink.isEmpty()) {
            logger.logInfo("Saving new message link for messageIds: {}", messageIds);
            messageLinkStorage.saveMessageLink(new MessageLink(UUID.randomUUID(), messageIds));
            return;
        }

        MessageLink messageLink = existingMessageLink.get();
        messageLink.addMessageIds(messageIds);
        logger.logInfo(
                "Updating existing message link {} with messageIds: {}",
                messageLink.getLinkId(),
                messageIds);
        messageLinkStorage.saveMessageLink(messageLink);
    }

    String[] getDataFromReportStream(String responseBody) throws FormatterProcessingException {
        // the expected json structure for the response is:
        // {
        //    ...
        //    "overallStatus": "Waiting to Deliver",
        //    "actualCompletionAt": "2023-10-24T19:48:26.921Z"
        //    ...
        //    "destinations" : [ {
        //        ...
        //        "organization_id" : "flexion",
        //        "service" : "simulated-lab",
        //        ...
        //    } ],
        //    ...
        //    "errors": [{
        //        "message": "some error message"
        //    }]
        // }

        Map<String, Object> responseObject =
                formatter.convertJsonToObject(responseBody, new TypeReference<>() {});

        String receiver;
        try {
            ArrayList<?> destinations = (ArrayList<?>) responseObject.get("destinations");
            Map<?, ?> destination = (Map<?, ?>) destinations.get(0);
            String organizationId = destination.get("organization_id").toString();
            String service = destination.get("service").toString();
            receiver = organizationId + "." + service;
        } catch (IndexOutOfBoundsException e) {
            // the destinations have not been determined yet by RS
            receiver = null;
        } catch (Exception e) {
            throw new FormatterProcessingException(
                    "Unable to extract receiver name from response due to unexpected format", e);
        }

        String overallStatus;
        try {
            overallStatus = (String) responseObject.get("overallStatus");
        } catch (Exception e) {
            throw new FormatterProcessingException(
                    "Unable to extract overallStatus from response due to unexpected format", e);
        }

        StringBuilder errorMessages = new StringBuilder();
        try {
            ArrayList<?> errors = (ArrayList<?>) responseObject.get("errors");
            for (Object error : errors) {
                Map<?, ?> x = (Map<?, ?>) error;
                errorMessages.append(x.get("message").toString()).append(" / ");
            }
        } catch (Exception e) {
            throw new FormatterProcessingException(
                    "Unable to extract failure reason due to unexpected format", e);
        }

        String timeDelivered = null;
        try {

            timeDelivered = (String) responseObject.get("actualCompletionAt");
        } catch (Exception e) {
            throw new FormatterProcessingException(
                    "Unable to extract timeDelivered due to unexpected format", e);
        }

        return new String[] {receiver, overallStatus, errorMessages.toString(), timeDelivered};
    }

    PartnerMetadataStatus ourStatusFromReportStreamStatus(String rsStatus) {
        if (rsStatus == null) {
            return PartnerMetadataStatus.PENDING;
        }

        // based off of the Status enum in the SubmissionHistory.kt code in RS
        // https://github.com/CDCgov/prime-reportstream/blob/master/prime-router/src/main/kotlin/history/SubmissionHistory.kt
        return switch (rsStatus) {
            case "Error", "Not Delivering" -> PartnerMetadataStatus.FAILED;
            case "Delivered" -> PartnerMetadataStatus.DELIVERED;
            default -> PartnerMetadataStatus.PENDING;
        };
    }

    private boolean metadataIsStale(PartnerMetadata partnerMetadata) {
        return partnerMetadata.receivingFacilityDetails().namespace() == null
                || partnerMetadata.deliveryStatus() == PartnerMetadataStatus.PENDING;
    }
}
