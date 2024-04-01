package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.SQLException;
import java.util.Set;
import javax.inject.Inject;

public class DatabaseLinkedMessageStorage {

    @Inject DbDao dao;

    @Inject Logger logger;

    private static final DatabaseLinkedMessageStorage INSTANCE = new DatabaseLinkedMessageStorage();

    private DatabaseLinkedMessageStorage() {}

    public static DatabaseLinkedMessageStorage getInstance() {
        return INSTANCE;
    }

    public MessageLink getMessageLink(String submissionId) throws Exception {
        try {
            return dao.fetchMessageLink(submissionId);
        } catch (SQLException e) {
            throw new Exception("Error retrieving metadata", e);
        }
    }

    public void saveLinkedMessages(Set<String> messageIds, int linkId)
            throws PartnerMetadataException {
        logger.logInfo("Saving message links");
        try {
            dao.insertMessageLink(messageIds, linkId);
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error saving message links", e);
        }
    }
}
