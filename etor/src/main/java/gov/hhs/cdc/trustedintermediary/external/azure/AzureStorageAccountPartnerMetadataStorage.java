package gov.hhs.cdc.trustedintermediary.external.azure;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;

/** Implements the {@link PartnerMetadataStorage} using files stored in an Azure Storage Account. */
public class AzureStorageAccountPartnerMetadataStorage implements PartnerMetadataStorage {

    private static final AzureStorageAccountPartnerMetadataStorage INSTANCE =
            new AzureStorageAccountPartnerMetadataStorage();

    private AzureStorageAccountPartnerMetadataStorage() {}

    public static AzureStorageAccountPartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
        return null;
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) {}
}
