package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;

/** Implements the {@link MessageLinkStorage} using a database. */
public class DatabaseMessageLinkStorage implements MessageLinkStorage {

    @Inject DbDao dao;

    @Inject Logger logger;

    private static final DatabaseMessageLinkStorage INSTANCE = new DatabaseMessageLinkStorage();

    private DatabaseMessageLinkStorage() {}

    public static DatabaseMessageLinkStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<MessageLink> getMessageLink(String submissionId) throws MessageLinkException {
        try {
            return dao.fetchMessageLink(submissionId);
        } catch (SQLException e) {
            throw new MessageLinkException("Error retrieving message links", e);
        }
    }

    @Override
    public void saveMessageLink(MessageLink messageLink) throws MessageLinkException {
        logger.logInfo("Saving message links");
        try {
            UUID linkId = messageLink.getLinkId();
            List<DbColumn> columns;
            for (String messageId : messageLink.getMessageIds()) {
                columns =
                        List.of(
                                new DbColumn("link_id", linkId, false, Types.VARCHAR),
                                new DbColumn("message_id", messageId, false, Types.VARCHAR));
                dao.upsertData("message_link", columns, "message_id");
            }
        } catch (SQLException e) {
            throw new MessageLinkException("Error saving message links", e);
        }
    }
}
