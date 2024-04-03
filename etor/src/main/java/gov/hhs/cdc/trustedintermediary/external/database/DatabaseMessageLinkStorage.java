package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.SQLException;
import java.util.Set;
import javax.inject.Inject;

public class DatabaseMessageLinkStorage implements MessageLinkStorage {

    @Inject DbDao dao;

    @Inject Logger logger;

    private static final DatabaseMessageLinkStorage INSTANCE = new DatabaseMessageLinkStorage();

    private DatabaseMessageLinkStorage() {}

    public static DatabaseMessageLinkStorage getInstance() {
        return INSTANCE;
    }

    public MessageLink getMessageLink(String submissionId) throws MessageLinkException {
        try {
            return dao.fetchMessageLink(submissionId);
        } catch (SQLException e) {
            throw new MessageLinkException("Error retrieving message links", e);
        }
    }

    public void saveMessageLink(Set<String> messageIds, int linkId) throws MessageLinkException {
        logger.logInfo("Saving message links");
        try {
            dao.insertMessageLink(messageIds, linkId);
        } catch (SQLException e) {
            throw new MessageLinkException("Error saving message links", e);
        }
    }
}
