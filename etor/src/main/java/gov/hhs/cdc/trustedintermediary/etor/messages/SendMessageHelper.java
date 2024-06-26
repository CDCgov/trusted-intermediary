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

    public void savePartnerMetadataForReceivedMessage(PartnerMetadata partnerMetadata) {
        if (partnerMetadata.receivedSubmissionId() == null) {
            logger.logWarning(
                    "Received submissionId is null so not saving metadata for received message");
            return;
        }
        try {
            partnerMetadataOrchestrator.updateMetadataForReceivedMessage(partnerMetadata);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to save metadata for receivedSubmissionId "
                            + partnerMetadata.receivedSubmissionId(),
                    e);
        }
    }

    public void saveSentMessageSubmissionId(String receivedSubmissionId, String sentSubmissionId) {
        if (sentSubmissionId == null || receivedSubmissionId == null) {
            logger.logWarning(
                    "Received and/or sent submissionId is null so not saving metadata for sent result");
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForSentMessage(
                    receivedSubmissionId, sentSubmissionId);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to update metadata for received submissionId "
                            + receivedSubmissionId
                            + " and sent submissionId "
                            + sentSubmissionId,
                    e);
        }
    }

    public void linkMessage(String receivedSubmissionId) {
        if (receivedSubmissionId == null) {
            logger.logWarning("Received submissionId is null so not linking messages");
            return;
        }

        try {
            Set<String> messageIdsToLink =
                    partnerMetadataOrchestrator.findMessagesIdsToLink(receivedSubmissionId);

            if (messageIdsToLink == null || messageIdsToLink.isEmpty()) {
                return;
            }

            // Add receivedSubmissionId to complete the list of messageIds to link
            messageIdsToLink.add(receivedSubmissionId);

            logger.logInfo(
                    "Found messages to link for receivedSubmissionId {}: {}",
                    receivedSubmissionId,
                    messageIdsToLink);
            partnerMetadataOrchestrator.linkMessages(messageIdsToLink);
        } catch (PartnerMetadataException | MessageLinkException e) {
            logger.logError(
                    "Unable to link messages for received submissionId " + receivedSubmissionId, e);
        }
    }
}
