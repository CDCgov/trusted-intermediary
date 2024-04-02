package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/** Interface for accessing the database for metadata */
public interface DbDao {
    void upsertData(String tableName, List<DbColumn> values, String conflictColumnName)
            throws SQLException;

    Object fetchMetadata(String uniqueId) throws SQLException;

    Set<PartnerMetadata> fetchMetadataForSender(String sender) throws SQLException;

    Set<PartnerMetadata> fetchMetadataForMessageLinking(String submissionId) throws SQLException;

    MessageLink fetchMessageLink(String messageId) throws SQLException;

    void insertMessageLink(Set<String> messageIds, int linkId) throws SQLException;
}
