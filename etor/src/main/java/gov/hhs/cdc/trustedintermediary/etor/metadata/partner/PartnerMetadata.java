package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import java.time.Instant;

/**
 * The partner-facing metadata.
 *
 * @param inboundReportId The inbound report ID.
 * @param sentSubmissionId The sent submission ID.
 * @param timeReceived The time the message was received.
 * @param timeDelivered The time the message was delivered.
 * @param hash The hash of the message.
 * @param deliveryStatus the status of the message based on an enum
 */
public record PartnerMetadata(
        String inboundReportId,
        String sentSubmissionId,
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
            String inboundReportId,
            String hash,
            PartnerMetadataMessageType messageType,
            MessageHdDataType sendingApplicationDetails,
            MessageHdDataType sendingFacilityDetails,
            MessageHdDataType receivingApplicationDetails,
            MessageHdDataType receivingFacilityDetails,
            String placerOrderNumber) {
        this(
                inboundReportId,
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

    public PartnerMetadata(String inboundReportId, PartnerMetadataStatus deliveryStatus) {
        this(
                inboundReportId,
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

    public PartnerMetadata withSentSubmissionId(String sentSubmissionId) {
        return new PartnerMetadata(
                this.inboundReportId,
                sentSubmissionId,
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
                this.inboundReportId,
                this.sentSubmissionId,
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
                this.inboundReportId,
                this.sentSubmissionId,
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
                this.inboundReportId,
                this.sentSubmissionId,
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
                this.inboundReportId,
                this.sentSubmissionId,
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
