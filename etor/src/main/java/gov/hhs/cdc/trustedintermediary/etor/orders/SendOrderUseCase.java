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

    public void convertAndSend(final Order<?> order, String receivedSubmissionId)
            throws UnableToSendOrderException {

        savePartnerMetadataForReceivedOrder(receivedSubmissionId, order);

        var omlOrder = converter.convertMetadataToOmlOrder(order);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.ORDER_CONVERTED_TO_OML);

        omlOrder = converter.addContactSectionToPatientResource(omlOrder);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT);

        String sentSubmissionId = sender.sendOrder(omlOrder).orElse(null);

        // TODO: may need to either wait or add a re-try mechanism if the receiver info
        //  is not yet available from the RS history API
        saveSentOrderSubmissionId(receivedSubmissionId, sentSubmissionId);
    }

    private void savePartnerMetadataForReceivedOrder(
            String receivedSubmissionId, final Order<?> order) {
        if (receivedSubmissionId == null) {
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForReceivedOrder(receivedSubmissionId, order);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to save metadata for receivedSubmissionId " + receivedSubmissionId, e);
        }
    }

    private void saveSentOrderSubmissionId(String receivedSubmissionId, String sentSubmissionId) {
        if (sentSubmissionId == null || receivedSubmissionId == null) {
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
