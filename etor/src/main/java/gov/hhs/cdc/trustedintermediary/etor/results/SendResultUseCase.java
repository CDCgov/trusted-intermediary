package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper;
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import javax.inject.Inject;

/** Use case for converting and sending a lab result message. */
public class SendResultUseCase implements SendMessageUseCase<Result<?>> {
    private static final SendResultUseCase INSTANCE = new SendResultUseCase();

    @Inject ResultConverter converter;
    @Inject ResultSender sender;
    @Inject MetricMetadata metadata;

    @Inject SendMessageHelper sendMessageHelper;

    @Inject Logger logger;

    private SendResultUseCase() {}

    public static SendResultUseCase getInstance() {
        return INSTANCE;
    }

    @Override
    public void convertAndSend(Result<?> result, String receivedSubmissionId)
            throws UnableToSendMessageException {

        PartnerMetadata partnerMetadata =
                new PartnerMetadata(
                        receivedSubmissionId,
                        String.valueOf(result.hashCode()),
                        PartnerMetadataMessageType.RESULT,
                        result.getSendingApplicationDetails(),
                        result.getSendingFacilityDetails(),
                        result.getReceivingApplicationDetails(),
                        result.getReceivingFacilityDetails(),
                        result.getPlacerOrderNumber());

        sendMessageHelper.savePartnerMetadataForReceivedMessage(partnerMetadata);

        var convertedResult = converter.addEtorProcessingTag(result);
        metadata.put(
                result.getFhirResourceId(),
                EtorMetadataStep.ETOR_PROCESSING_TAG_ADDED_TO_MESSAGE_HEADER);

        String outboundReportId = sender.send(convertedResult).orElse(null);
        logger.logInfo("Sent result reportId: {}", outboundReportId);

        sendMessageHelper.linkMessage(receivedSubmissionId);

        sendMessageHelper.saveSentMessageSubmissionId(receivedSubmissionId, outboundReportId);
    }
}
