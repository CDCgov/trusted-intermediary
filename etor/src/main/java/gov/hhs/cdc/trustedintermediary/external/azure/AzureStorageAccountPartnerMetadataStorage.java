package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.core.exception.AzureException;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using files stored in an Azure Storage Account. */
public class AzureStorageAccountPartnerMetadataStorage implements PartnerMetadataStorage {

    private static final String STORAGE_ACCOUNT_BLOB_ENDPOINT =
            ApplicationContext.getProperty("STORAGE_ACCOUNT_BLOB_ENDPOINT");
    private static final String METADATA_CONTAINER_NAME =
            ApplicationContext.getProperty("METADATA_CONTAINER_NAME");

    private BlobContainerClient containerClient;

    private static final AzureStorageAccountPartnerMetadataStorage INSTANCE =
            new AzureStorageAccountPartnerMetadataStorage();

    AzureStorageAccountPartnerMetadataStorage(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    @Inject Formatter formatter;
    @Inject Logger logger;

    private AzureStorageAccountPartnerMetadataStorage() {}

    public static AzureStorageAccountPartnerMetadataStorage getInstance() {
        INSTANCE.containerClient =
                new BlobServiceClientBuilder()
                        .endpoint(STORAGE_ACCOUNT_BLOB_ENDPOINT)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient()
                        .getBlobContainerClient(METADATA_CONTAINER_NAME);
        return INSTANCE;
    }

    @Override
    public PartnerMetadata readMetadata(final String uniqueId) throws PartnerMetadataException {
        String metadataFileName = getMetadataFileName(uniqueId);
        logger.logInfo("Reading metadata for " + metadataFileName);
        try {
            BlobClient blobClient = containerClient.getBlobClient(metadataFileName);
            logger.logInfo("Reading metadata from " + blobClient.getBlobUrl());
            String content = blobClient.downloadContent().toString();
            return formatter.convertJsonToObject(content, new TypeReference<>() {});
        } catch (AzureException | FormatterProcessingException e) {
            throw new PartnerMetadataException("Unable to download " + metadataFileName, e);
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        String metadataFileName = getMetadataFileName(metadata.uniqueId());
        logger.logInfo("Saving metadata for " + metadataFileName);
        try {
            BlobClient blobClient = containerClient.getBlobClient(metadataFileName);
            String content = formatter.convertToJsonString(metadata);
            blobClient.upload(BinaryData.fromString(content));
            logger.logInfo("Saved metadata to " + blobClient.getBlobUrl());
        } catch (AzureException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Failed to upload "
                            + metadataFileName
                            + " to "
                            + METADATA_CONTAINER_NAME
                            + " container",
                    e);
        }
    }

    public static String getMetadataFileName(String uniqueId) {
        return uniqueId + ".json";
    }
}
