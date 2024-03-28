package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using a database. */
public class DatabasePartnerMetadataStorage implements PartnerMetadataStorage {

    @Inject DbDao dao;

    @Inject Logger logger;
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
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        logger.logInfo("saving the metadata");
        List<DbColumn> values = new ArrayList<>();

        var dbColumn =
                new DbColumn(
                        "received_message_id",
                        metadata.receivedSubmissionId(),
                        false,
                        Types.VARCHAR);
        values.add(dbColumn);

        DbColumn dbColumn2 =
                new DbColumn("sent_message_id", metadata.sentSubmissionId(), true, Types.VARCHAR);
        values.add(dbColumn2);

        DbColumn dbColumn3 = new DbColumn("sender", metadata.sender(), false, Types.VARCHAR);
        values.add(dbColumn3);

        DbColumn dbColumn4 = new DbColumn("receiver", metadata.receiver(), true, Types.VARCHAR);
        values.add(dbColumn4);

        DbColumn dbColumn5 = new DbColumn("hash_of_order", metadata.hash(), false, Types.VARCHAR);
        values.add(dbColumn5);

        Timestamp timestampReceived = null;
        if (metadata.timeReceived() != null) {
            timestampReceived = Timestamp.from(metadata.timeReceived());
        }
        DbColumn dbColumn6 =
                new DbColumn("time_received", timestampReceived, false, Types.TIMESTAMP);
        values.add(dbColumn6);

        Timestamp timestampDelivered = null;
        if (metadata.timeDelivered() != null) {
            timestampDelivered = Timestamp.from(metadata.timeDelivered());
        }
        DbColumn dbColumn7 =
                new DbColumn("time_delivered", timestampDelivered, true, Types.TIMESTAMP);
        values.add(dbColumn7);

        String deliveryStatusString = null;
        if (metadata.deliveryStatus() != null) {
            deliveryStatusString = metadata.deliveryStatus().toString();
        }
        DbColumn dbColumn8 =
                new DbColumn("delivery_status", deliveryStatusString, true, Types.OTHER);
        values.add(dbColumn8);

        DbColumn dbColumn9 =
                new DbColumn("failure_reason", metadata.failureReason(), true, Types.VARCHAR);
        values.add(dbColumn9);

        String messageTypeString = null;
        if (metadata.messageType() != null) {
            messageTypeString = metadata.messageType().toString();
        }
        DbColumn dbColumn10 = new DbColumn("message_type", messageTypeString, false, Types.OTHER);
        values.add(dbColumn10);

        try {
            dao.upsertData("metadata", values, "received_message_id");
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error saving metadata", e);
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
        }
        return consolidatedMetadata;
    }
}
