package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/** Interface for accessing the database for metadata */
public interface DbDao {
    void upsertMetadata(
            String receivedId,
            String sentId,
            String sender,
            String receiver,
            String hash,
            Instant timeReceived,
            Instant timeDelivered,
            PartnerMetadataStatus deliveryStatus,
            String failureReason,
            PartnerMetadataMessageType messageType)
            throws SQLException;

    Object fetchMetadata(String uniqueId) throws SQLException;

    Set<PartnerMetadata> fetchMetadataForSender(String sender) throws SQLException;

    Set<PartnerMetadata> fetchMetadataForMessageLinking(String submissionId) throws SQLException;

    Set<MessageLink> fetchLinkedMessages(String messageId) throws SQLException;

    void insertLinkedMessages(Set<String> messageIds, Optional<Integer> linkId) throws SQLException;
}
