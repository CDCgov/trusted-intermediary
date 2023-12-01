package gov.hhs.cdc.trustedintermediary.etor.metadata;

import java.time.LocalTime;

public record PartnerMetadata(
        String uniqueId, String sender, String receiver, LocalTime timeReceived, String hash) {}
