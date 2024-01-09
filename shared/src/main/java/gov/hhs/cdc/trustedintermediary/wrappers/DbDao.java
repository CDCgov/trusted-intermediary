package gov.hhs.cdc.trustedintermediary.wrappers;

import java.sql.SQLException;
import java.time.Instant;

/** Interface for accessing the database for metadata */
public interface DbDao {
    void upsertMetadata(
            String receivedId,
            String sentId,
            String sender,
            String receiver,
            String hash,
            Instant timeReceived)
            throws SQLException;

    Object fetchMetadata(String uniqueId) throws SQLException;
}
