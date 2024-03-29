package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/** Interface for accessing the database for metadata */
public interface DbDao {
    void upsertData(String tableName, List<DbColumn> values, String conflictColumnName)
            throws SQLException;

    Set<PartnerMetadata> fetchMetadataForSender(String sender) throws SQLException;

    Object fetchMetadata(String uniqueId) throws SQLException;
}
