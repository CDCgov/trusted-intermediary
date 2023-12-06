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
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    @Inject Formatter formatter;
    @Inject Logger logger;

    private AzureStorageAccountPartnerMetadataStorage() {}

    public static AzureStorageAccountPartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public PartnerMetadata readMetadata(final String uniqueId) throws PartnerMetadataException {
        String metadataFileName = uniqueId + ".json";
        try {
            logger.logDebug("Downloading " + metadataFileName);
            BlobClient blobClient = CONTAINER_CLIENT.getBlobClient(metadataFileName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);
            String content = outputStream.toString(StandardCharsets.UTF_8);
            logger.logDebug("Downloaded metadata: " + content);
            return formatter.convertJsonToObject(content, new TypeReference<>() {});
        } catch (AzureException | FormatterProcessingException e) {
            throw new PartnerMetadataException("Unable to download " + metadataFileName, e);
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        String metadataFileName = metadata.uniqueId() + ".json";
        try {
            BlobClient blobClient = CONTAINER_CLIENT.getBlobClient(metadataFileName);
            String content = formatter.convertToJsonString(metadata);
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
        } catch (AzureException | FormatterProcessingException e) {
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
