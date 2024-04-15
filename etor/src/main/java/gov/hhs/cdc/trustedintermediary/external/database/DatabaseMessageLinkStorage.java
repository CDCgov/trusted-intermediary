package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;

/** Implements the {@link MessageLinkStorage} using a database. */
public class DatabaseMessageLinkStorage implements MessageLinkStorage {

    @Inject DbDao dao;

    @Inject ConnectionPool connectionPool;

    @Inject Logger logger;

    private static final DatabaseMessageLinkStorage INSTANCE = new DatabaseMessageLinkStorage();

    private DatabaseMessageLinkStorage() {}

    public static DatabaseMessageLinkStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<MessageLink> getMessageLink(String messageId) throws MessageLinkException {
        var sql =
                """
                SELECT *
                FROM message_link
                WHERE message_id = ?;
                """;

        try {
            UUID linkId = null;
            Set<String> messageIds = new HashSet<>();
            try (Connection conn = connectionPool.getConnection();
                    PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, messageId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        if (linkId == null) {
                            linkId = UUID.fromString(resultSet.getString("link_id"));
                        }
                        messageIds.add(resultSet.getString("message_id"));
                    }
                }
            }

            if (!messageIds.isEmpty()) {
                return Optional.of(new MessageLink(linkId, messageIds));
            } else {
                return Optional.empty();
            }
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
