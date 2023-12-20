package gov.hhs.cdc.trustedintermediary.etor.metadata;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;

public class PartnerMetadataOrchestrator {

    private static final PartnerMetadataOrchestrator INSTANCE = new PartnerMetadataOrchestrator();

    public static PartnerMetadataOrchestrator getInstance() {
        return INSTANCE;
    }

    private PartnerMetadataOrchestrator() {}

    public void updateMetadataForReceivedOrder(String submissionId, Order<?> order) {}

    public void updateMetadataForSentOrder(
            String receivedSubmissionId, String sentSubmissionId, Order<?> order) {}

    public PartnerMetadata getMetadata(String submissionId) {
        return null;
    }
}
