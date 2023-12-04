package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetaDataStorage;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;

/** Implements the {@link PartnerMetaDataStorage} using a database. */
public class DatabasePartnerMetaDataStorage implements PartnerMetaDataStorage {
    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
        return null;
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) {}
}
