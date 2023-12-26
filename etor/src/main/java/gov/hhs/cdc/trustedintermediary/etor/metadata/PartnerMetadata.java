package gov.hhs.cdc.trustedintermediary.etor.metadata;

import java.time.Instant;

/**
 * The partner-facing metadata.
 *
 * @param receivedSubmissionId The received submission ID.
 * @param sentSubmissionId The sent submission ID.
 * @param sender The name of the sender of the message.
 * @param receiver The name of the receiver of the message.
 * @param timeReceived The time the message was received.
 * @param hash The hash of the message.
 */
public record PartnerMetadata(
        String receivedSubmissionId,
        String sentSubmissionId,
        String sender,
        String receiver,
        Instant timeReceived,
        String hash) {
    public PartnerMetadata(String receivedSubmissionId) {
        this(receivedSubmissionId, null, null, null, null, null);
    }
}
