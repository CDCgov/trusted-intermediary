package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import java.time.Instant;

/**
 * The partner-facing metadata.
 *
 * @param outboundMessageId The outbound message ID.
 * @param inboundMessageId The inbound message ID.
 * @param timeReceived The time the message was received.
 * @param timeDelivered The time the message was delivered.
 * @param hash The hash of the message.
 * @param deliveryStatus the status of the message based on an enum
 */
public record PartnerMetadata(
        String outboundMessageId,
        String inboundMessageId,
        Instant timeReceived,
        Instant timeDelivered,
        String hash,
        PartnerMetadataStatus deliveryStatus,
        String failureReason,
        PartnerMetadataMessageType messageType,
        MessageHdDataType sendingApplicationDetails,
        MessageHdDataType sendingFacilityDetails,
        MessageHdDataType receivingApplicationDetails,
        MessageHdDataType receivingFacilityDetails,
        String placerOrderNumber) {

    // Below is for defaulting status when null
    public PartnerMetadata {
        if (deliveryStatus == null) {
            deliveryStatus = PartnerMetadataStatus.PENDING;
        }
    }

    public PartnerMetadata(
            String outboundMessageId,
            String hash,
            PartnerMetadataMessageType messageType,
            MessageHdDataType sendingApplicationDetails,
            MessageHdDataType sendingFacilityDetails,
            MessageHdDataType receivingApplicationDetails,
            MessageHdDataType receivingFacilityDetails,
            String placerOrderNumber) {
        this(
                outboundMessageId,
                null,
                null,
                null,
                hash,
                null,
                null,
                messageType,
                sendingApplicationDetails,
                sendingFacilityDetails,
                receivingApplicationDetails,
                receivingFacilityDetails,
                placerOrderNumber);
    }

    public PartnerMetadata(String outboundMessageId, PartnerMetadataStatus deliveryStatus) {
        this(
                outboundMessageId,
                null,
                null,
                null,
                null,
                deliveryStatus,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public PartnerMetadata withInboundMessageId(String inboundMessageId) {
        return new PartnerMetadata(
                this.outboundMessageId,
                inboundMessageId,
                this.timeReceived,
                this.timeDelivered,
                this.hash,
                this.deliveryStatus,
                this.failureReason,
                this.messageType,
                this.sendingApplicationDetails,
                this.sendingFacilityDetails,
                this.receivingApplicationDetails,
                this.receivingFacilityDetails,
                this.placerOrderNumber);
    }

    public PartnerMetadata withTimeReceived(Instant timeReceived) {
        return new PartnerMetadata(
                this.outboundMessageId,
                this.inboundMessageId,
                timeReceived,
                this.timeDelivered,
                this.hash,
                this.deliveryStatus,
                this.failureReason,
                this.messageType,
                this.sendingApplicationDetails,
                this.sendingFacilityDetails,
                this.receivingApplicationDetails,
                this.receivingFacilityDetails,
                this.placerOrderNumber);
    }

    public PartnerMetadata withTimeDelivered(Instant timeDelivered) {
        return new PartnerMetadata(
                this.outboundMessageId,
                this.inboundMessageId,
                this.timeReceived,
                timeDelivered,
                this.hash,
                this.deliveryStatus,
                this.failureReason,
                this.messageType,
                this.sendingApplicationDetails,
                this.sendingFacilityDetails,
                this.receivingApplicationDetails,
                this.receivingFacilityDetails,
                this.placerOrderNumber);
    }

    public PartnerMetadata withDeliveryStatus(PartnerMetadataStatus deliveryStatus) {
        return new PartnerMetadata(
                this.outboundMessageId,
                this.inboundMessageId,
                this.timeReceived,
                this.timeDelivered,
                this.hash,
                deliveryStatus,
                this.failureReason,
                this.messageType,
                this.sendingApplicationDetails,
                this.sendingFacilityDetails,
                this.receivingApplicationDetails,
                this.receivingFacilityDetails,
                this.placerOrderNumber);
    }

    public PartnerMetadata withFailureMessage(String failureMessage) {
        return new PartnerMetadata(
                this.outboundMessageId,
                this.inboundMessageId,
                this.timeReceived,
                this.timeDelivered,
                this.hash,
                this.deliveryStatus,
                failureMessage,
                this.messageType,
                this.sendingApplicationDetails,
                this.sendingFacilityDetails,
                this.receivingApplicationDetails,
                this.receivingFacilityDetails,
                this.placerOrderNumber);
    }
}
