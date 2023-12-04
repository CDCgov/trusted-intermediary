package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;

/** Implements the {@link PartnerMetadataStorage} using local files. */
public class FilePartnerMetadataStorage implements PartnerMetadataStorage {
    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
        return null;
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) {}
}
