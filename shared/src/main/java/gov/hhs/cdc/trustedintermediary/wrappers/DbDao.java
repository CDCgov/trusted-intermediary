package gov.hhs.cdc.trustedintermediary.wrappers;

import java.sql.SQLException;
import java.time.Instant;

public interface DbDao {
    void connect() throws SQLException;

    void upsertMetadata(
            String id, String sender, String receiver, String hash, Instant timeReceived);
}
