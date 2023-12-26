package gov.hhs.cdc.trustedintermediary.etor.metadata;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
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
        // call the metadata storage and add the sent order's submission ID to the existing metadata
        // entry
        // PartnerMetadata may need to be updated to store both the received order's submission ID
        // _and_ the sent order's submission ID.
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
}
