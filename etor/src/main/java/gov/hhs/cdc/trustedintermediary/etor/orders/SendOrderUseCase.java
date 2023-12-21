package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataOrchestrator;
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

    public void convertAndSend(final Order<?> order, String submissionId)
            throws UnableToSendOrderException {

        var partnerMetadata =
                new PartnerMetadata(
                        "uniqueId",
                        "senderName",
                        "receiverName",
                        Instant.now(),
                        "abcd"); // TODO: delete once PR is ready
        try {
            logger.logInfo("Trying to save the metadata");
            savePartnerMetadata(submissionId);
            logger.logInfo("Trying to read the metadata"); // TODO: delete once PR is ready
            partnerMetadataStorage.readMetadata(
                    partnerMetadata.uniqueId()); // TODO: delete once PR is ready

        } catch (PartnerMetadataException e) {
            logger.logError("Unable to save metadata for submissionId " + submissionId, e);
        }

        var omlOrder = converter.convertMetadataToOmlOrder(order);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.ORDER_CONVERTED_TO_OML);
        omlOrder = converter.addContactSectionToPatientResource(omlOrder);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT);
        sender.sendOrder(omlOrder);

        saveSentOrderSubmissionId(
                submissionId,
                "TBD, need to be filled in from the sender.sendOrder(omlOrder) call",
                order);
    }

    private void savePartnerMetadataForReceivedOrder(String submissionId, final Order<?> order) {
        if (submissionId == null) {
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForReceivedOrder(submissionId, order);
        } catch (PartnerMetadataException e) {
            logger.logError("Unable to save metadata for submissionId " + submissionId, e);
        }
    }

    private void saveSentOrderSubmissionId(
            String receivedSubmissionId, String sentSubmissionId, final Order<?> order) {
        if (sentSubmissionId == null || receivedSubmissionId == null) {
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForSentOrder(
                    receivedSubmissionId, sentSubmissionId, order);
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
