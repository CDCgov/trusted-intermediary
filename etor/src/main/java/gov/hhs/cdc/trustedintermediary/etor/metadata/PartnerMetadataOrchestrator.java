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
        // will call the RS history API given the submissionId, currently blocked by:
        // https://github.com/CDCgov/prime-reportstream/issues/12624
        // from the response, extract the "sender" and "timestamp" (timeReceived)
        // we will calculate the hash.
        // then we call the metadata storage to save this stuff.

        String sender = "unknown";
        Instant timeReceived = Instant.now();
        String hash = String.valueOf(order.hashCode());
        PartnerMetadata partnerMetadata =
                new PartnerMetadata(receivedSubmissionId, sender, timeReceived, hash);
        partnerMetadataStorage.saveMetadata(partnerMetadata);
    }

    public void updateMetadataForSentOrder(String receivedSubmissionId, String sentSubmissionId)
            throws PartnerMetadataException {

        PartnerMetadata partnerMetadata =
                partnerMetadataStorage.readMetadata(receivedSubmissionId).orElseThrow();
        if (sentSubmissionId != null
                && !sentSubmissionId.equals(partnerMetadata.sentSubmissionId())) {
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
        // call the metadata storage to get the metadata.
        // check if the receiver is filled out, and if it isn't, call the RS history API to get the
        // receiver.
        // if had to call the history API, extract the receiver and call the metadata storage to
        // save the metadata with the receiver added.
        // return the metadata.
        return partnerMetadataStorage.readMetadata(receivedSubmissionId);
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
