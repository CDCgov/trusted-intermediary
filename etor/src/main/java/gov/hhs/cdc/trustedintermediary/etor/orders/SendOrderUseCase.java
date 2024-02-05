package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import javax.inject.Inject;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();
    @Inject OrderConverter converter;
    @Inject OrderSender sender;
    @Inject MetricMetadata metadata;
    @Inject PartnerMetadataOrchestrator partnerMetadataOrchestrator;
    @Inject Logger logger;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    public void convertAndSend(final Order<?> order, String receivedSubmissionId)
            throws UnableToSendMessageException {

        savePartnerMetadataForReceivedOrder(receivedSubmissionId, order);

        var omlOrder = converter.convertToOmlOrder(order);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.ORDER_CONVERTED_TO_OML);

        omlOrder = converter.addContactSectionToPatientResource(omlOrder);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT);

        omlOrder = converter.addEtorProcessingTag(omlOrder);
        metadata.put(
                order.getFhirResourceId(),
                EtorMetadataStep.ETOR_PROCESSING_TAG_ADDED_TO_MESSAGE_HEADER);

        String sentSubmissionId = sender.send(omlOrder).orElse(null);

        saveSentOrderSubmissionId(receivedSubmissionId, sentSubmissionId);
    }

    private void savePartnerMetadataForReceivedOrder(
            String receivedSubmissionId, final Order<?> order) {
        if (receivedSubmissionId == null) {
            logger.logWarning(
                    "Received submissionId is null so not saving metadata for received order");
            return;
        }

        try {
            String orderHash = String.valueOf(order.hashCode());
            partnerMetadataOrchestrator.updateMetadataForReceivedOrder(
                    receivedSubmissionId, orderHash);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to save metadata for receivedSubmissionId " + receivedSubmissionId, e);
        }
    }

    private void saveSentOrderSubmissionId(String receivedSubmissionId, String sentSubmissionId) {
        if (sentSubmissionId == null || receivedSubmissionId == null) {
            logger.logWarning(
                    "Received and/or sent submissionId is null so not saving metadata for sent order");
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForSentOrder(
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
}
