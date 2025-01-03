package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper;
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine;
import gov.hhs.cdc.trustedintermediary.etor.utils.security.HashHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import javax.inject.Inject;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase implements SendMessageUseCase<Order<?>> {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();
    @Inject TransformationRuleEngine transformationEngine;
    @Inject OrderSender sender;
    @Inject MetricMetadata metadata;
    @Inject SendMessageHelper sendMessageHelper;
    @Inject Logger logger;
    @Inject HashHelper hashHelper;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    @Override
    public void convertAndSend(final Order<?> order, String inboundReportId)
            throws UnableToSendMessageException {

        String hashedOrder = hashHelper.generateHash(order);

        PartnerMetadata partnerMetadata =
                new PartnerMetadata(
                        inboundReportId,
                        hashedOrder,
                        PartnerMetadataMessageType.ORDER,
                        order.getSendingApplicationDetails(),
                        order.getSendingFacilityDetails(),
                        order.getReceivingApplicationDetails(),
                        order.getReceivingFacilityDetails(),
                        order.getPlacerOrderNumber());

        sendMessageHelper.savePartnerMetadataForInboundMessage(partnerMetadata);

        transformationEngine.runRules(order);

        String outboundReportId = sender.send(order).orElse(null);
        logger.logInfo("Sent order reportId: {}", outboundReportId);

        sendMessageHelper.linkMessage(inboundReportId);

        sendMessageHelper.saveOutboundReportId(inboundReportId, outboundReportId);
    }
}
