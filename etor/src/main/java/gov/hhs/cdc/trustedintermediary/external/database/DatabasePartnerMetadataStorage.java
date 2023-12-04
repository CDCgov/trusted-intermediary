package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;

/** Implements the {@link PartnerMetadataStorage} using a database. */
public class DatabasePartnerMetadataStorage implements PartnerMetadataStorage {
    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
        return null;
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) {}
}
