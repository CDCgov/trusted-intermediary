package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetaDataStorage;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;

/** Implements the {@link PartnerMetaDataStorage} using local files. */
public class FilePartnerMetaDataStorage implements PartnerMetaDataStorage {
    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
        return null;
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) {}
}
