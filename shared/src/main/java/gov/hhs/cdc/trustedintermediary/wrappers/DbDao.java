package gov.hhs.cdc.trustedintermediary.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public interface DbDao {
    void upsertMetadata(
            String id, String sender, String receiver, String hash, Instant timeReceived)
            throws SQLException;

    ResultSet fetchMetadata(String lookupValue);
}
