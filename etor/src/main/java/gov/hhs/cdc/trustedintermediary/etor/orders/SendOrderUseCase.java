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

        savePartnerMetadataForReceivedOrder(submissionId, order);

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
