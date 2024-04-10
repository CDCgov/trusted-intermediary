package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using a database. */
public class DatabasePartnerMetadataStorage implements PartnerMetadataStorage {

    @Inject DbDao dao;

    @Inject Logger logger;

    @Inject Formatter formatter;
    private static final DatabasePartnerMetadataStorage INSTANCE =
            new DatabasePartnerMetadataStorage();

    private DatabasePartnerMetadataStorage() {}

    public static DatabasePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<PartnerMetadata> readMetadata(final String uniqueId)
            throws PartnerMetadataException {
        try {
            PartnerMetadata data = (PartnerMetadata) dao.fetchMetadata(uniqueId);
            return Optional.ofNullable(data);
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error retrieving metadata", e);
        } catch (FormatterProcessingException e) {
            throw new PartnerMetadataException("Error formatting metadata", e);
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
                                Types.OTHER),
                        new DbColumn(
                                "placer_order_number",
                                metadata.placerOrderNumber(),
                                false,
                                Types.VARCHAR),
                        new DbColumn(
                                "sending_application_details",
                                metadata.sendingApplicationDetails(),
                                false,
                                Types.OTHER),
                        new DbColumn(
                                "sending_facility_details",
                                metadata.sendingFacilityDetails(),
                                false,
                                Types.OTHER),
                        new DbColumn(
                                "receiving_application_details",
                                metadata.receivingApplicationDetails(),
                                false,
                                Types.OTHER),
                        new DbColumn(
                                "receiving_facility_details",
                                metadata.receivingFacilityDetails(),
                                false,
                                Types.OTHER));
        try {
            dao.upsertData("metadata", columns, "received_message_id");
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
            consolidatedMetadata = dao.fetchMetadataForSender(sender);
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error retrieving consolidated metadata", e);
        } catch (FormatterProcessingException e) {
            throw new PartnerMetadataException("Error formatting consolidated metadata", e);
        }
        return consolidatedMetadata;
    }
}
