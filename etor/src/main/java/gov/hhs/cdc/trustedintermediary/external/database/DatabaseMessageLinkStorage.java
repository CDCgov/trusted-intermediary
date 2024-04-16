package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

/** Implements the {@link MessageLinkStorage} using a database. */
public class DatabaseMessageLinkStorage implements MessageLinkStorage {

    private static final DatabaseMessageLinkStorage INSTANCE = new DatabaseMessageLinkStorage();

    @Inject DbDao dao;

    @Inject Logger logger;

    private DatabaseMessageLinkStorage() {}

    public static DatabaseMessageLinkStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<MessageLink> getMessageLink(String messageId) throws MessageLinkException {

        try {
            Set<Map<UUID, String>> partialMessageLinks =
                    dao.fetchManyData(
                            connection -> {
                                try {
                                    PreparedStatement statement =
                                            connection.prepareStatement(
                                                    """
                                    SELECT *
                                    FROM message_link
                                    WHERE message_id = ?;
                                    """);
                                    statement.setString(1, messageId);
                                    return statement;
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            this::partialMessageLinkFromResultSet,
                            Collectors.toSet());

            final MessageLink messageLink = buildMessageLinkFromPartials(partialMessageLinks);

            return Optional.ofNullable(messageLink);
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
                dao.upsertData(
                        "message_link",
                        columns,
                        "ON CONSTRAINT message_link_link_id_message_id_key");
            }
        } catch (SQLException e) {
            throw new MessageLinkException("Error saving message links", e);
        }
    }

    Map<UUID, String> partialMessageLinkFromResultSet(ResultSet resultSet) {
        try {
            UUID linkId = UUID.fromString(resultSet.getString("link_id"));
            String messageId = resultSet.getString("message_id");
            return Map.of(linkId, messageId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    MessageLink buildMessageLinkFromPartials(final Set<Map<UUID, String>> partialMessageLinks) {
        UUID linkId = null;
        Set<String> messageIds = new HashSet<>();

        for (Map<UUID, String> partialMessageLink : partialMessageLinks) {
            for (UUID id : partialMessageLink.keySet()) {
                if (linkId == null) {
                    linkId = id;
                }
                messageIds.add(partialMessageLink.get(id));
            }
        }

        MessageLink messageLink = null;
        if (linkId != null) {
            messageLink = new MessageLink(linkId, messageIds);
        }

        return messageLink;
    }
}
