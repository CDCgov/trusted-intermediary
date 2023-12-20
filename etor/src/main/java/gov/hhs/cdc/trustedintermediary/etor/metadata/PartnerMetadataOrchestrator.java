package gov.hhs.cdc.trustedintermediary.etor.metadata;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;

public class PartnerMetadataOrchestrator {

    private static final PartnerMetadataOrchestrator INSTANCE = new PartnerMetadataOrchestrator();

    public static PartnerMetadataOrchestrator getInstance() {
        return INSTANCE;
    }

    private PartnerMetadataOrchestrator() {}

    public void updateMetadataForReceivedOrder(String submissionId, Order<?> order) {
        // will call the RS history API given the submissionId (albeit, right now this won't work
        // given the way RS works).
        // from the history API response, extract the sender (organization + sender client), and
        // time received.
        // we will calculate the hash.
        // then we call the metadata storage to save this stuff.
    }

    public void updateMetadataForSentOrder(
            String receivedSubmissionId, String sentSubmissionId, Order<?> order) {
        // call the metadata storage and add the sent order's submission ID to the existing metadata
        // entry
    }

    public PartnerMetadata getMetadata(String submissionId) {
        // call the metadata storage to get the metadata.
        // check if the receiver is filled out, and if it isn't, call the RS history API to get the
        // receiver.
        // if had to call the history API, extract the receiver and call the metadata storage to
        // save the metadata with the receiver added.
        // return the metadata.
        return null;
    }
}
