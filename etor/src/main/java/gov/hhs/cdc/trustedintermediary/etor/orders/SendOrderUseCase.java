package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataOrchestrator;
import gov.hhs.cdc.trustedintermediary.utils.RetryTask;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Inject OrderConverter converter;
    @Inject OrderSender sender;
    @Inject MetricMetadata metadata;
    @Inject PartnerMetadataOrchestrator partnerMetadataOrchestrator;
    @Inject Logger logger;
    @Inject RetryTask retryTask;

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
            partnerMetadataOrchestrator.updateMetadataForReceivedOrder(receivedSubmissionId, order);
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
        Callable<Void> task =
                () -> {
                    partnerMetadataOrchestrator.updateMetadataForSentOrder(
                            receivedSubmissionId, sentSubmissionId);
                    return null;
                };
        retryTask.scheduleRetry(task, 1, 1000);
    }
}
