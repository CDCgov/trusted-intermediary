package gov.hhs.cdc.trustedintermediary.etor.messages;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Set;
import javax.inject.Inject;

public class SendMessageHelper {
    private static final SendMessageHelper INSTANCE = new SendMessageHelper();

    public static SendMessageHelper getInstance() {
        return INSTANCE;
    }

    @Inject Logger logger;

    @Inject PartnerMetadataOrchestrator partnerMetadataOrchestrator;

    private SendMessageHelper() {}

    public void savePartnerMetadataForInboundMessage(PartnerMetadata partnerMetadata) {
        if (partnerMetadata.inboundReportId() == null) {
            logger.logWarning(
                    "inboundReportId is null so not saving metadata for received message");
            return;
        }
        try {
            partnerMetadataOrchestrator.updateMetadataForInboundMessage(partnerMetadata);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to save metadata for inboundReportId "
                            + partnerMetadata.inboundReportId(),
                    e);
        }
    }

    public void saveReportIds(String inboundReportId, String outboundReportId) {
        if (outboundReportId == null || inboundReportId == null) {
            logger.logWarning(
                    "Inbound and/or outboundReportId is null so not saving metadata for sent result");
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForOutboundMessage(
                    inboundReportId, outboundReportId);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to update metadata for inboundReportId "
                            + inboundReportId
                            + " and sent outboundReportId "
                            + outboundReportId,
                    e);
        }
    }

    public void linkMessage(String inboundReportId) {
        if (inboundReportId == null) {
            logger.logWarning("inboundReportId is null so not linking messages");
            return;
        }

        try {
            Set<String> messageIdsToLink =
                    partnerMetadataOrchestrator.findMessagesIdsToLink(inboundReportId);

            if (messageIdsToLink == null || messageIdsToLink.isEmpty()) {
                return;
            }

            // Add inboundReportId to complete the list of messageIds to link
            messageIdsToLink.add(inboundReportId);

            logger.logInfo(
                    "Found messages to link for inboundReportId {}: {}",
                    inboundReportId,
                    messageIdsToLink);
            partnerMetadataOrchestrator.linkMessages(messageIdsToLink);
        } catch (PartnerMetadataException | MessageLinkException e) {
            logger.logError("Unable to link messages for inboundReportId " + inboundReportId, e);
        }
    }
}
