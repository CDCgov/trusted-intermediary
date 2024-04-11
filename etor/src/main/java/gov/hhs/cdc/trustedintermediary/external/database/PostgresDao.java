package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

/** Class for accessing and managing data for the postgres Database */
public class PostgresDao implements DbDao {

    private static final PostgresDao INSTANCE = new PostgresDao();

    @Inject ConnectionPool connectionPool;
    @Inject Logger logger;

    @Inject Formatter formatter;

    private PostgresDao() {}

    public static PostgresDao getInstance() {
        return INSTANCE;
    }

    @Override
    public void upsertData(String tableName, List<DbColumn> values, String conflictColumnName)
            throws SQLException {
        // example SQL statement generated here:
        // INSERT INTO metadata_table (column_one, column_three, column_two, column_four)
        // VALUES (?, ?, ?, ?)
        // ON CONFLICT (column_one) DO UPDATE SET column_three = EXCLUDED.column_three, column_two =
        // EXCLUDED.column_two

        StringBuilder sqlStatementBuilder =
                new StringBuilder("INSERT INTO ").append(tableName).append(" (");

        values.forEach(dbColumn -> sqlStatementBuilder.append(dbColumn.name()).append(", "));
        removeLastTwoCharacters(sqlStatementBuilder); // remove the last unused ", "

        sqlStatementBuilder.append(") VALUES (");

        sqlStatementBuilder.append("?, ".repeat(values.size()));
        removeLastTwoCharacters(sqlStatementBuilder); // remove the last unused ", "
        sqlStatementBuilder.append(")");

        boolean wantsUpsert = values.stream().anyMatch(DbColumn::upsertOverwrite);

        if (wantsUpsert) {
            sqlStatementBuilder
                    .append(" ON CONFLICT (")
                    .append(conflictColumnName)
                    .append(") DO UPDATE SET ");

            for (DbColumn column : values) {
                if (!column.upsertOverwrite()) {
                    continue;
                }

                sqlStatementBuilder.append(column.name()).append(" = EXCLUDED.");
                sqlStatementBuilder.append(column.name());
                sqlStatementBuilder.append(", ");
            }

            removeLastTwoCharacters(sqlStatementBuilder); // remove the last unused ", "
        }

        String sqlStatement = sqlStatementBuilder.toString();

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = conn.prepareStatement(sqlStatement)) {

            for (int i = 0; i < values.size(); i++) {
                DbColumn column = values.get(i);
                Object value = column.value();
                int type = column.type();

                if (value != null) {
                    statement.setObject(i + 1, value, type);
                } else {
                    statement.setNull(i + 1, type);
                }
            }

            statement.executeUpdate();
        }
    }

    @Override
    public Set<PartnerMetadata> fetchMetadataForSender(String sender)
            throws SQLException, FormatterProcessingException {

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement =
                        conn.prepareStatement("SELECT * FROM metadata WHERE sender = ?")) {
            statement.setString(1, sender);
            ResultSet resultSet = statement.executeQuery();

            Set<PartnerMetadata> metadataSet = new HashSet<>();

            while (resultSet.next()) {
                metadataSet.add(partnerMetadataFromResultSet(resultSet));
            }

            return metadataSet;
        }
    }

    @Override
    public PartnerMetadata fetchMetadata(String submissionId)
            throws SQLException, FormatterProcessingException {
        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement =
                        conn.prepareStatement(
                                "SELECT * FROM metadata where received_message_id = ? OR sent_message_id = ?")) {

            statement.setString(1, submissionId);
            statement.setString(2, submissionId);

            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                return null;
            }

            return partnerMetadataFromResultSet(result);
        }
    }

    @Override
    public Set<PartnerMetadata> fetchMetadataForMessageLinking(String submissionId)
            throws SQLException, FormatterProcessingException {
        var sql =
                """
                SELECT m2.*
                FROM metadata m1
                JOIN metadata m2
                    ON m1.placer_order_number = m2.placer_order_number
                        AND m1.sending_application_id = m2.sending_application_id
                        AND m1.sending_facility_id = m2.sending_facility_id
                WHERE m1.sent_message_id = ?;
                """;

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, submissionId);
            ResultSet resultSet = statement.executeQuery();

            Set<PartnerMetadata> metadataSet = new HashSet<>();

            while (resultSet.next()) {
                metadataSet.add(partnerMetadataFromResultSet(resultSet));
            }

            return metadataSet;
        }
    }

    @Override
    public Optional<MessageLink> fetchMessageLink(String messageId) throws SQLException {
        var sql =
                """
                SELECT *
                FROM message_link
                WHERE message_id = ?;
                """;

        int linkId = -1;
        Set<String> messageIds = new HashSet<>();
        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, messageId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    if (linkId == -1) {
                        linkId = resultSet.getInt("link_id");
                    }
                    messageIds.add(resultSet.getString("message_id"));
                }
            }
        }

        if (!messageIds.isEmpty() && linkId != -1) {
            return Optional.of(new MessageLink(linkId, messageIds));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void insertMessageLink(MessageLink messageLink) throws SQLException {
        var getMaxLinkIdSql =
                "SELECT COALESCE(MAX(link_id), 0) + 1 AS next_link_id FROM message_link";
        var insertSql =
                """
                INSERT INTO message_link (link_id, message_id)
                SELECT ?, ?
                WHERE NOT EXISTS (
                    SELECT 1 FROM message_link WHERE message_id = ?
                );
                """;

        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            // creating a transaction to ensure that the insert is atomic and avoid race condition
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            Integer linkId = messageLink.getLinkId();
            if (linkId == null) {
                try (Statement getMaxLinkIdStatement = conn.createStatement();
                        ResultSet result = getMaxLinkIdStatement.executeQuery(getMaxLinkIdSql)) {
                    if (result.next()) {
                        linkId = result.getInt("next_link_id"); // Retrieve the next linkId
                    }
                }
            }

            if (linkId == null) {
                throw new SQLException("Failed to retrieve the next linkId");
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(insertSql)) {
                for (String messageId : messageLink.getMessageIds()) {
                    insertStatement.setInt(1, linkId);
                    insertStatement.setString(2, messageId);
                    insertStatement.setString(3, messageId); // can we use named parameters instead?
                    insertStatement.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException exRollback) {
                    logger.logError("Failed to rollback transaction", exRollback);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException exClose) {
                    logger.logError("Failed to close the connection", exClose);
                }
            }
        }
    }

    private void removeLastTwoCharacters(StringBuilder stringBuilder) {
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
    }

    private PartnerMetadata partnerMetadataFromResultSet(ResultSet resultSet)
            throws SQLException, FormatterProcessingException {
        Instant timeReceived = null;
        Instant timeDelivered = null;
        Timestamp timestampReceived = resultSet.getTimestamp("time_received");
        Timestamp timestampDelivered = resultSet.getTimestamp("time_delivered");
        if (timestampReceived != null) {
            timeReceived = timestampReceived.toInstant();
        }

        if (timestampDelivered != null) {
            timeDelivered = timestampDelivered.toInstant();
        }

        return new PartnerMetadata(
                resultSet.getString("received_message_id"),
                resultSet.getString("sent_message_id"),
                resultSet.getString("sender"),
                resultSet.getString("receiver"),
                timeReceived,
                timeDelivered,
                resultSet.getString("hash_of_message"),
                PartnerMetadataStatus.valueOf(resultSet.getString("delivery_status")),
                resultSet.getString("failure_reason"),
                PartnerMetadataMessageType.valueOf(resultSet.getString("message_type")),
                formatter.convertJsonToObject(
                        resultSet.getString("sending_application_details"),
                        new TypeReference<>() {}),
                formatter.convertJsonToObject(
                        resultSet.getString("sending_facility_details"), new TypeReference<>() {}),
                formatter.convertJsonToObject(
                        resultSet.getString("receiving_application_details"),
                        new TypeReference<>() {}),
                formatter.convertJsonToObject(
                        resultSet.getString("receiving_facility_details"),
                        new TypeReference<>() {}),
                resultSet.getString("placer_order_number"));
    }
}
