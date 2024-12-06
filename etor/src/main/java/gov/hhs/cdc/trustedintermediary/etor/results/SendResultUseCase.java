package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper;
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine;
import gov.hhs.cdc.trustedintermediary.etor.utils.security.HashHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;

/** Use case for converting and sending a lab result message. */
public class SendResultUseCase implements SendMessageUseCase<Result<?>> {
    private static final SendResultUseCase INSTANCE = new SendResultUseCase();

    @Inject TransformationRuleEngine transformationEngine;
    @Inject ResultSender sender;

    @Inject SendMessageHelper sendMessageHelper;

    @Inject Logger logger;

    @Inject HashHelper hashHelper;

    private SendResultUseCase() {}

    public static SendResultUseCase getInstance() {
        return INSTANCE;
    }

    @Override
    public void convertAndSend(Result<?> result, String inboundReportId)
            throws UnableToSendMessageException {

        String hashedResult = hashHelper.generateHash(result);

        PartnerMetadata partnerMetadata =
                new PartnerMetadata(
                        inboundReportId,
                        hashedResult,
                        PartnerMetadataMessageType.RESULT,
                        result.getSendingApplicationDetails(),
                        result.getSendingFacilityDetails(),
                        result.getReceivingApplicationDetails(),
                        result.getReceivingFacilityDetails(),
                        result.getPlacerOrderNumber());

        sendMessageHelper.savePartnerMetadataForInboundMessage(partnerMetadata);

        transformationEngine.runRules(result);

        String outboundReportId = sender.send(result).orElse(null);
        logger.logInfo("Sent result outboundReportId: {}", outboundReportId);

        sendMessageHelper.linkMessage(inboundReportId);

        sendMessageHelper.saveOutboundReportId(inboundReportId, outboundReportId);
    }
}
