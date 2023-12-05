package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.core.exception.AzureException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using files stored in an Azure Storage Account. */
public class AzureStorageAccountPartnerMetadataStorage implements PartnerMetadataStorage {

    private static final String STORAGE_ACCOUNT_BLOB_ENDPOINT =
            ApplicationContext.getProperty("STORAGE_ACCOUNT_BLOB_ENDPOINT");
    private static final String METADATA_CONTAINER_NAME =
            ApplicationContext.getProperty("METADATA_CONTAINER_NAME");
    private static final BlobContainerClient CONTAINER_CLIENT =
            new BlobServiceClientBuilder()
                    .endpoint(STORAGE_ACCOUNT_BLOB_ENDPOINT)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient()
                    .getBlobContainerClient(METADATA_CONTAINER_NAME);

    private static final AzureStorageAccountPartnerMetadataStorage INSTANCE =
            new AzureStorageAccountPartnerMetadataStorage();

    @Inject Logger logger;

    private AzureStorageAccountPartnerMetadataStorage() {}

    public static AzureStorageAccountPartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
        return null;
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        String metadataFileName = metadata.uniqueId() + ".json";
        try {
            BlobClient blobClient = CONTAINER_CLIENT.getBlobClient(metadataFileName);
            String content = "serialize(metadata)";
            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            blobClient.upload(inputStream, content.length(), true);
            logger.logInfo(
                    "Saved metadata for "
                            + metadata.uniqueId()
                            + " to "
                            + METADATA_CONTAINER_NAME
                            + " container in "
                            + STORAGE_ACCOUNT_BLOB_ENDPOINT
                            + " Azure storage account"
                            + blobClient.getBlobUrl());
        } catch (AzureException e) {
            throw new PartnerMetadataException(
                    "Failed to upload "
                            + metadataFileName
                            + " in "
                            + METADATA_CONTAINER_NAME
                            + " container",
                    e);
        }
    }
}
