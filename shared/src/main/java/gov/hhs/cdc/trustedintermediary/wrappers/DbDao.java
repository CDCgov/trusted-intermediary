package gov.hhs.cdc.trustedintermediary.wrappers;

import java.time.Instant;

public interface DbDao {
    void connect();

    void upsertMetadata(
            String id, String sender, String receiver, String hash, Instant timeReceived);
}
