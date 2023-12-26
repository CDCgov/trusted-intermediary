package gov.hhs.cdc.trustedintermediary.etor.metadata;

import java.util.Optional;

/** Interface to store and retrieve our partner-facing metadata. */
public interface PartnerMetadataStorage {

    /**
     * This method will retrieve and return the metadata for the given submissionId, if it exists.
     *
     * @param submissionId The submission Id to read the metadata for.
     * @return The metadata, if it exists. Otherwise, an empty Optional.
     */
    Optional<PartnerMetadata> readMetadata(String submissionId) throws PartnerMetadataException;

    /**
     * This method will do "upserts". If the record doesn't exist, it is created. If the record
     * exists, it is updated.
     *
     * @param metadata The metadata to save.
     */
    void saveMetadata(PartnerMetadata metadata) throws PartnerMetadataException;
}
