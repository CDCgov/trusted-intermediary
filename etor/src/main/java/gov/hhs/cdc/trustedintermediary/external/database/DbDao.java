package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus;

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
            Instant timeReceived,
            PartnerMetadataStatus deliveryStatus)
            throws SQLException;

    Object fetchMetadata(String uniqueId) throws SQLException;
}
