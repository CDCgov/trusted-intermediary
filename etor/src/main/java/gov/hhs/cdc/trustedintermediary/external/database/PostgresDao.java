package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

/** Class for accessing and managing data for the postgres Database */
public class PostgresDao implements DbDao {

    private static final PostgresDao INSTANCE = new PostgresDao();

    @Inject Logger logger;
    @Inject ConnectionPool connectionPool;

    private PostgresDao() {}

    public static PostgresDao getInstance() {
        return INSTANCE;
    }

    @Override
    public void upsertMetadata(
            String receivedSubmissionId,
            String sentSubmissionId,
            String sender,
            String receiver,
            String hash,
            Instant timeReceived,
            Instant timeDelivered,
            PartnerMetadataStatus deliveryStatus,
            String failureReason)
            throws SQLException {

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement =
                        conn.prepareStatement(
                                """
                                INSERT INTO metadata VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                                ON CONFLICT (received_message_id) DO UPDATE SET receiver = EXCLUDED.receiver, sent_message_id = EXCLUDED.sent_message_id, time_delivered = EXCLUDED.time_delivered, delivery_status = EXCLUDED.delivery_status, failure_reason = EXCLUDED.failure_reason
                                """)) {

            statement.setString(1, receivedSubmissionId);
            statement.setString(2, sentSubmissionId);
            statement.setString(3, sender);
            statement.setString(4, receiver);
            statement.setString(5, hash);

            Timestamp timestampReceived = null;
            if (timeReceived != null) {
                timestampReceived = Timestamp.from(timeReceived);
            }
            statement.setTimestamp(6, timestampReceived);

            Timestamp timestampDelivered = null;

            if (timeDelivered != null) {
                timestampDelivered = Timestamp.from(timeDelivered);
            }
            statement.setTimestamp(7, timestampDelivered);

            String deliveryStatusString = null;
            if (deliveryStatus != null) {
                deliveryStatusString = deliveryStatus.toString();
            }

            statement.setObject(8, deliveryStatusString, Types.OTHER);

            statement.setString(9, failureReason);

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
                resultSet.getString("hash_of_order"),
                PartnerMetadataStatus.valueOf(resultSet.getString("delivery_status")),
                resultSet.getString("failure_reason"));
    }
}
