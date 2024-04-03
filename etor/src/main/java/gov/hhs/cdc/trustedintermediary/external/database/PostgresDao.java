package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus;
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

/** Class for accessing and managing data for the postgres Database */
public class PostgresDao implements DbDao {

    private static final PostgresDao INSTANCE = new PostgresDao();

    @Inject ConnectionPool connectionPool;

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
    public Set<PartnerMetadata> fetchMetadataForSender(String sender) throws SQLException {

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
    public PartnerMetadata fetchMetadata(String submissionId) throws SQLException {
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
            throws SQLException {
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
    public MessageLink fetchMessageLink(String messageId) throws SQLException {
        var sql =
                """
                SELECT *
                FROM message_link
                WHERE message_id = ?;
                """;

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, messageId);

            try (ResultSet result = statement.executeQuery()) {
                return new MessageLink(
                        result.getInt("id"),
                        result.getInt("link_id"),
                        result.getString("message_id"));
            }
        }
    }

    @Override
    public void insertMessageLink(Set<String> messageIds, int linkId) throws SQLException {
        // todo: still need to deal with race condition
        var sql =
                """
                INSERT INTO message_link (link_id, message_id)
                VALUES (
                    COALESCE(
                        ?,
                        (SELECT COALESCE(MAX(link_id), 0) + 1 FROM message_link)
                    ),
                    ?
                );
                """;

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {

            for (String messageId : messageIds) {
                statement.setInt(1, linkId);
                statement.setString(2, messageId);
                statement.executeUpdate();
            }
        }
    }

    private void removeLastTwoCharacters(StringBuilder stringBuilder) {
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
    }

    private PartnerMetadata partnerMetadataFromResultSet(ResultSet resultSet) throws SQLException {
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
                resultSet.getString("sending_application_id"),
                resultSet.getString("sending_facility_id"),
                resultSet.getString("receiving_application_id"),
                resultSet.getString("receiving_facility_id"),
                resultSet.getString("placer_order_number"));
    }
}
