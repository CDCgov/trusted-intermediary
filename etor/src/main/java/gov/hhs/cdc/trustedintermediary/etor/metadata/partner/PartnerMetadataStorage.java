package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

import java.util.Optional;
import java.util.Set;

/** Interface to store and retrieve our partner-facing metadata. */
public interface PartnerMetadataStorage {

    /**
     * This method will retrieve and return the metadata for the given submissionId, if it exists.
     *
     * @param receivedSubmissionId The submission Id to read the metadata for.
     * @return The metadata, if it exists. Otherwise, an empty Optional.
     */
    Optional<PartnerMetadata> readMetadata(String receivedSubmissionId)
            throws PartnerMetadataException;

    /**
     * This method will do "upserts". If the record doesn't exist, it is created. If the record
     * exists, it is updated.
     *
     * @param metadata The metadata to save.
     */
    void saveMetadata(PartnerMetadata metadata) throws PartnerMetadataException;

    /**
     * This method will return a list of IDs along with the associated status and any status
     * messages
     *
     * @param sender the name of the sender to search for
     * @return a map of all submission ids
     */
    Set<PartnerMetadata> readMetadataForSender(String sender) throws PartnerMetadataException;
}
