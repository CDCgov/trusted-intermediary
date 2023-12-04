package gov.hhs.cdc.trustedintermediary.etor.metadata;

import java.time.LocalTime;

/**
 * The partner-facing metadata.
 *
 * @param uniqueId The unique ID that identifies this specific metadata.
 * @param sender The name of the sender of the message.
 * @param receiver The name of the receiver of the message.
 * @param timeReceived The time the message was received.
 * @param hash The hash of the message.
 */
public record PartnerMetadata(
        String uniqueId, String sender, String receiver, LocalTime timeReceived, String hash) {}
