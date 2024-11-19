package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper;
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;
import org.apache.commons.codec.binary.Hex;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase implements SendMessageUseCase<Order<?>> {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();
    @Inject TransformationRuleEngine transformationEngine;
    @Inject OrderSender sender;
    @Inject MetricMetadata metadata;
    @Inject SendMessageHelper sendMessageHelper;
    @Inject Logger logger;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    @Override
    public void convertAndSend(final Order<?> order, String receivedSubmissionId)
            throws UnableToSendMessageException {

        String hash = generateHash(order);

        PartnerMetadata partnerMetadata =
                new PartnerMetadata(
                        receivedSubmissionId,
                        hash,
                        PartnerMetadataMessageType.ORDER,
                        order.getSendingApplicationDetails(),
                        order.getSendingFacilityDetails(),
                        order.getReceivingApplicationDetails(),
                        order.getReceivingFacilityDetails(),
                        order.getPlacerOrderNumber());

        sendMessageHelper.savePartnerMetadataForReceivedMessage(partnerMetadata);

        transformationEngine.runRules(order);

        String outboundReportId = sender.send(order).orElse(null);
        logger.logInfo("Sent order reportId: {}", outboundReportId);

        sendMessageHelper.linkMessage(receivedSubmissionId);

        sendMessageHelper.saveSentMessageSubmissionId(receivedSubmissionId, outboundReportId);
    }

    public String generateHash(final Order<?> order) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] objBytes = order.toString().getBytes(StandardCharsets.UTF_8);
            byte[] hashBytes = digest.digest(objBytes);
            return Hex.encodeHexString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.logError("Algorithm does not exist!", e);
        }
        return "";
    }
}
