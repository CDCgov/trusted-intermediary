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

    public void savePartnerMetadataForOutboundMessage(PartnerMetadata partnerMetadata) {
        if (partnerMetadata.outboundMessageId() == null) {
            logger.logWarning(
                    "Outbound messageId is null so not saving metadata for outbound message");
            return;
        }
        try {
            partnerMetadataOrchestrator.updateMetadataForOutboundMessage(partnerMetadata);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to save metadata for outboundMessageId "
                            + partnerMetadata.outboundMessageId(),
                    e);
        }
    }

    public void saveInboundMessageId(String outboundMessageId, String inboundMessageId) {
        if (inboundMessageId == null || outboundMessageId == null) {
            logger.logWarning(
                    "Outbound and/or inbound messageId is null so not saving metadata for sent result");
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForInboundMessage(
                    outboundMessageId, inboundMessageId);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to update metadata for outbound messageId "
                            + outboundMessageId
                            + " and inbound messageId "
                            + inboundMessageId,
                    e);
        }
    }

    public void linkMessage(String outboundMessageId) {
        if (outboundMessageId == null) {
            logger.logWarning("Outbound messageId is null so not linking messages");
            return;
        }

        try {
            Set<String> messageIdsToLink =
                    partnerMetadataOrchestrator.findMessagesIdsToLink(outboundMessageId);

            if (messageIdsToLink == null || messageIdsToLink.isEmpty()) {
                return;
            }

            // Add outboundMessageId to complete the list of messageIds to link
            messageIdsToLink.add(outboundMessageId);

            logger.logInfo(
                    "Found messages to link for outboundMessageId {}: {}",
                    outboundMessageId,
                    messageIdsToLink);
            partnerMetadataOrchestrator.linkMessages(messageIdsToLink);
        } catch (PartnerMetadataException | MessageLinkException e) {
            logger.logError(
                    "Unable to link messages for outbound messageId " + outboundMessageId, e);
        }
    }
}
