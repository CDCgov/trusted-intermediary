package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
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

    private static final String METADATA_TABLE_RECEIVED_MESSAGE_ID = "received_message_id";

    @Inject DbDao dao;

    @Inject Logger logger;

    @Inject Formatter formatter;

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

        try {
            List<DbColumn> columns = createDbColumnsFromMetadata(metadata);
            dao.upsertData("metadata", columns, "(" + METADATA_TABLE_RECEIVED_MESSAGE_ID + ")");
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error saving metadata", e);
        } catch (FormatterProcessingException e) {
            throw new PartnerMetadataException("Error parsing metadata", e);
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

    @Override
    public Set<PartnerMetadata> readMetadataForMessageLinking(String submissionId)
            throws PartnerMetadataException {

        Set<PartnerMetadata> metadataSet;
        try {
            metadataSet =
                    dao.fetchManyData(
                            connection -> {
                                try {
                                    PreparedStatement statement =
                                            connection.prepareStatement(
                                                    """
                                    SELECT m2.*
                                    FROM metadata m1
                                    JOIN metadata m2
                                        ON m1.placer_order_number = m2.placer_order_number
                                            AND m1.sending_application_id = m2.sending_application_id
                                            AND m1.sending_facility_id = m2.sending_facility_id
                                    WHERE m1.sent_message_id = ?;
                                    """);
                                    statement.setString(1, submissionId);
                                    return statement;
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            this::partnerMetadataFromResultSet,
                            Collectors.toSet());

            return metadataSet;
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error retrieving metadata", e);
        }
    }

    PartnerMetadata partnerMetadataFromResultSet(ResultSet resultSet) {
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
                    resultSet.getString(METADATA_TABLE_RECEIVED_MESSAGE_ID),
                    resultSet.getString("sent_message_id"),
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
                            resultSet.getString("sending_facility_details"),
                            new TypeReference<>() {}),
                    formatter.convertJsonToObject(
                            resultSet.getString("receiving_application_details"),
                            new TypeReference<>() {}),
                    formatter.convertJsonToObject(
                            resultSet.getString("receiving_facility_details"),
                            new TypeReference<>() {}),
                    resultSet.getString("placer_order_number"));
        } catch (SQLException | FormatterProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DbColumn> createDbColumnsFromMetadata(PartnerMetadata metadata)
            throws FormatterProcessingException {
        return List.of(
                new DbColumn(
                        METADATA_TABLE_RECEIVED_MESSAGE_ID,
                        metadata.receivedSubmissionId(),
                        false,
                        Types.VARCHAR),
                new DbColumn("sent_message_id", metadata.sentSubmissionId(), true, Types.VARCHAR),
                new DbColumn(
                        "sender",
                        metadata.sendingFacilityDetails().namespace(),
                        false,
                        Types.VARCHAR),
                new DbColumn(
                        "receiver",
                        metadata.receivingFacilityDetails().namespace(),
                        true,
                        Types.VARCHAR),
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
                        "delivery_status", metadata.deliveryStatus().toString(), true, Types.OTHER),
                new DbColumn("failure_reason", metadata.failureReason(), true, Types.VARCHAR),
                new DbColumn(
                        "message_type",
                        metadata.messageType() != null ? metadata.messageType().toString() : null,
                        false,
                        Types.OTHER),
                new DbColumn(
                        "placer_order_number", metadata.placerOrderNumber(), false, Types.VARCHAR),
                new DbColumn(
                        "sending_application_details",
                        formatter.convertToJsonString(metadata.sendingApplicationDetails()),
                        false,
                        Types.OTHER),
                new DbColumn(
                        "sending_facility_details",
                        formatter.convertToJsonString(metadata.sendingFacilityDetails()),
                        false,
                        Types.OTHER),
                new DbColumn(
                        "receiving_application_details",
                        formatter.convertToJsonString(metadata.receivingApplicationDetails()),
                        false,
                        Types.OTHER),
                new DbColumn(
                        "receiving_facility_details",
                        formatter.convertToJsonString(metadata.receivingFacilityDetails()),
                        false,
                        Types.OTHER));
    }
}
