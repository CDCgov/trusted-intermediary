package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    @Inject RSEndpointClient rsclient;
    @Inject Formatter formatter;
    @Inject Logger logger;

    public static PartnerMetadataOrchestrator getInstance() {
        return INSTANCE;
    }

    private PartnerMetadataOrchestrator() {}

    public void updateMetadataForReceivedOrder(String receivedSubmissionId, String orderHash)
            throws PartnerMetadataException {
        // currently blocked by: https://github.com/CDCgov/prime-reportstream/issues/12624
        // once we get the right receivedSubmissionId from RS, this method should work
        logger.logInfo(
                "Looking up sender name and timeReceived from RS history API for receivedSubmissionId: {}",
                receivedSubmissionId);

        String sender;
        Instant timeReceived;

        try {
            String bearerToken = rsclient.getRsToken();
            String responseBody =
                    rsclient.requestHistoryEndpoint(receivedSubmissionId, bearerToken);
            Map<String, Object> responseObject =
                    formatter.convertJsonToObject(responseBody, new TypeReference<>() {});

            sender = responseObject.get("sender").toString();
            String timestamp = responseObject.get("timestamp").toString();
            timeReceived = Instant.parse(timestamp);

        } catch (Exception e) {
            // write the received submission ID so that the rest of the metadata flow works even if
            // some data is missing
            logger.logWarning(
                    "Unable to retrieve metadata from RS history API, but writing basic metadata entry anyway for received submission ID {}",
                    receivedSubmissionId);
            PartnerMetadata partnerMetadata = new PartnerMetadata(receivedSubmissionId, orderHash);
            partnerMetadataStorage.saveMetadata(partnerMetadata);

            throw new PartnerMetadataException(
                    "Unable to retrieve metadata from RS history API", e);
        }

        logger.logInfo(
                "Updating metadata with sender: {}, timeReceived: {} and hash",
                sender,
                timeReceived);
        PartnerMetadata partnerMetadata =
                new PartnerMetadata(
                        receivedSubmissionId,
                        sender,
                        timeReceived,
                        orderHash,
                        PartnerMetadataStatus.PENDING);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public void updateMetadataForSentOrder(String receivedSubmissionId, String sentSubmissionId)
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
            try {
                String bearerToken = rsclient.getRsToken();
                String responseBody =
                        rsclient.requestHistoryEndpoint(sentSubmissionId, bearerToken);
                var parsedResponseBody = getDataFromReportStream(responseBody);
                receiver = parsedResponseBody[0];
                rsStatus = parsedResponseBody[1];
                rsMessage = parsedResponseBody[2];
            } catch (ReportStreamEndpointClientException | FormatterProcessingException e) {
                throw new PartnerMetadataException(
                        "Unable to retrieve metadata from RS history API", e);
            }

            var ourStatus = ourStatusFromReportStreamStatus(rsStatus);

            logger.logInfo("Updating metadata with receiver {} and status {}", receiver, ourStatus);
            partnerMetadata = partnerMetadata.withReceiver(receiver).withDeliveryStatus(ourStatus);

            if (ourStatus == PartnerMetadataStatus.FAILED) {
                partnerMetadata = partnerMetadata.withFailureMessage(rsMessage);
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

    public Map<String, Map<String, String>> getConsolidatedMetadata(String senderName)
            throws PartnerMetadataException {

        var metadataSet = partnerMetadataStorage.readMetadataForSender(senderName);

        return metadataSet.stream()
                .collect(
                        Collectors.toMap(
                                PartnerMetadata::receivedSubmissionId,
                                metadata -> {
                                    var status = String.valueOf(metadata.deliveryStatus());
                                    var stale = metadataIsStale(metadata) ? "💩" : "✅";
                                    var failureReason = metadata.failureReason();

                                    Map<String, String> innerMap = new HashMap<>();
                                    innerMap.put("status", status);
                                    innerMap.put("stale", stale);
                                    innerMap.put("failureReason", failureReason);

                                    return innerMap;
                                }));
    }

    String[] getDataFromReportStream(String responseBody) throws FormatterProcessingException {
        // the expected json structure for the response is:
        // {
        //    ...
        //    "overallStatus": "Waiting to Deliver",
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

        return new String[] {receiver, overallStatus, errorMessages.toString()};
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
        return partnerMetadata.receiver() == null
                || partnerMetadata.deliveryStatus() == PartnerMetadataStatus.PENDING;
    }
}
