package gov.hhs.cdc.trustedintermediary.external.azure;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetaDataStorage;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;

/** Implements the {@link PartnerMetaDataStorage} using files stored in an Azure Storage Account. */
public class AzureStorageAccountPartnerMetaDataStorage implements PartnerMetaDataStorage {
    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
        return null;
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) {}
}
