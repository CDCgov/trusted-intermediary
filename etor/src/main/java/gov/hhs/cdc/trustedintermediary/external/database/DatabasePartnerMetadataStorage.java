package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using a database. */
public class DatabasePartnerMetadataStorage implements PartnerMetadataStorage {

    private static final DatabasePartnerMetadataStorage INSTANCE =
            new DatabasePartnerMetadataStorage();

    @Inject DbDao dao;

    @Inject Logger logger;

    private DatabasePartnerMetadataStorage() {}

    public static DatabasePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<PartnerMetadata> readMetadata(final String uniqueId)
            throws PartnerMetadataException {
        try {
            PartnerMetadata metadata =
                    dao.fetchFirstData(
                            connection -> {
                                try {
                                    PreparedStatement statement =
                                            connection.prepareStatement(
                                                    "SELECT * FROM metadata where received_message_id = ? OR sent_message_id = ?");
                                    statement.setString(1, uniqueId);
                                    statement.setString(2, uniqueId);
                                    return statement;
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            this::partnerMetadataFromResultSet);

            return Optional.ofNullable(metadata);
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error retrieving metadata", e);
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        logger.logInfo("saving the metadata");

        List<DbColumn> columns =
                List.of(
                        new DbColumn(
                                "received_message_id",
                                metadata.receivedSubmissionId(),
                                false,
                                Types.VARCHAR),
                        new DbColumn(
                                "sent_message_id",
                                metadata.sentSubmissionId(),
                                true,
                                Types.VARCHAR),
                        new DbColumn("sender", metadata.sender(), false, Types.VARCHAR),
                        new DbColumn("receiver", metadata.receiver(), true, Types.VARCHAR),
                        new DbColumn("hash_of_message", metadata.hash(), false, Types.VARCHAR),
                        new DbColumn(
                                "time_received",
                                metadata.timeReceived() != null
                                        ? Timestamp.from(metadata.timeReceived())
                                        : null,
                                false,
                                Types.TIMESTAMP),
                        new DbColumn(
                                "time_delivered",
                                metadata.timeDelivered() != null
                                        ? Timestamp.from(metadata.timeDelivered())
                                        : null,
                                true,
                                Types.TIMESTAMP),
                        new DbColumn(
                                "delivery_status",
                                metadata.deliveryStatus().toString(),
                                true,
                                Types.OTHER),
                        new DbColumn(
                                "failure_reason", metadata.failureReason(), true, Types.VARCHAR),
                        new DbColumn(
                                "message_type",
                                metadata.messageType() != null
                                        ? metadata.messageType().toString()
                                        : null,
                                false,
                                Types.OTHER));

        try {
            dao.upsertData("metadata", columns, "received_message_id");
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error saving metadata", e);
        }
    }

    @Override
    public Set<PartnerMetadata> readMetadataForSender(String sender)
            throws PartnerMetadataException {
        Set<PartnerMetadata> consolidatedMetadata;
        try {
            consolidatedMetadata =
                    dao.fetchManyData(
                            connection -> {
                                try {
                                    PreparedStatement statement =
                                            connection.prepareStatement(
                                                    "SELECT * FROM metadata WHERE sender = ?");
                                    statement.setString(1, sender);
                                    return statement;
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            this::partnerMetadataFromResultSet,
                            Collectors.toSet());

            return consolidatedMetadata;

        } catch (SQLException e) {
            throw new PartnerMetadataException("Error retrieving consolidated metadata", e);
        }
    }

    private PartnerMetadata partnerMetadataFromResultSet(ResultSet resultSet) {
        try {
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
                    PartnerMetadataMessageType.valueOf(resultSet.getString("message_type")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
