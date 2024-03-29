package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

import java.time.Instant;

/**
 * The partner-facing metadata.
 *
 * @param receivedSubmissionId The received submission ID.
 * @param sentSubmissionId The sent submission ID.
 * @param sender The name of the sender of the message.
 * @param receiver The name of the receiver of the message.
 * @param timeReceived The time the message was received.
 * @param timeDelivered The time the message was delivered.
 * @param hash The hash of the message.
 * @param deliveryStatus the status of the message based on an enum
 */
public record PartnerMetadata(
        String receivedSubmissionId,
        String sentSubmissionId,
        String sender,
        String receiver,
        Instant timeReceived,
        Instant timeDelivered,
        String hash,
        PartnerMetadataStatus deliveryStatus,
        String failureReason,
        PartnerMetadataMessageType messageType) {

    // Below is for defaulting status when null
    public PartnerMetadata {
        if (deliveryStatus == null) {
            deliveryStatus = PartnerMetadataStatus.PENDING;
        }
    }

    public PartnerMetadata(
            String receivedSubmissionId,
            String sender,
            Instant timeReceived,
            Instant timeDelivered,
            String hash,
            PartnerMetadataStatus deliveryStatus,
            PartnerMetadataMessageType messageType) {
        this(
                receivedSubmissionId,
                null,
                sender,
                null,
                timeReceived,
                timeDelivered,
                hash,
                deliveryStatus,
                null,
                messageType);
    }

    public PartnerMetadata(String receivedSubmissionId, String hash) {
        this(receivedSubmissionId, null, null, null, null, null, hash, null, null, null);
    }

    public PartnerMetadata(
            String receivedSubmissionId, String hash, PartnerMetadataMessageType messageType) {
        this(receivedSubmissionId, null, null, null, null, null, hash, null, null, messageType);
    }

    public PartnerMetadata(String receivedSubmissionId, PartnerMetadataStatus deliveryStatus) {
        this(receivedSubmissionId, null, null, null, null, null, null, deliveryStatus, null, null);
    }

    public PartnerMetadata(
            String receivedSubmissionId,
            PartnerMetadataStatus deliveryStatus,
            PartnerMetadataMessageType messageType) {
        this(
                receivedSubmissionId,
                null,
                null,
                null,
                null,
                null,
                null,
                deliveryStatus,
                null,
                messageType);
    }

    public PartnerMetadata withSentSubmissionId(String sentSubmissionId) {
        return new PartnerMetadata(
                this.receivedSubmissionId,
                sentSubmissionId,
                this.sender,
                this.receiver,
                this.timeReceived,
                this.timeDelivered,
                this.hash,
                this.deliveryStatus,
                this.failureReason,
                this.messageType);
    }

    public PartnerMetadata withReceiver(String receiver) {
        return new PartnerMetadata(
                this.receivedSubmissionId,
                this.sentSubmissionId,
                this.sender,
                receiver,
                this.timeReceived,
                this.timeDelivered,
                this.hash,
                this.deliveryStatus,
                this.failureReason,
                this.messageType);
    }

    public PartnerMetadata withTimeDelivered(Instant timeDelivered) {
        return new PartnerMetadata(
                this.receivedSubmissionId,
                this.sentSubmissionId,
                this.sender,
                this.receiver,
                this.timeReceived,
                timeDelivered,
                this.hash,
                this.deliveryStatus,
                this.failureReason,
                this.messageType);
    }

    public PartnerMetadata withDeliveryStatus(PartnerMetadataStatus deliveryStatus) {
        return new PartnerMetadata(
                this.receivedSubmissionId,
                this.sentSubmissionId,
                this.sender,
                this.receiver,
                this.timeReceived,
                this.timeDelivered,
                this.hash,
                deliveryStatus,
                this.failureReason,
                this.messageType);
    }

    public PartnerMetadata withFailureMessage(String failureMessage) {
        return new PartnerMetadata(
                this.receivedSubmissionId,
                this.sentSubmissionId,
                this.sender,
                this.receiver,
                this.timeReceived,
                this.timeDelivered,
                this.hash,
                this.deliveryStatus,
                failureMessage,
                this.messageType);
    }
}
