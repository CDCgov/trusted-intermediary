package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.core.exception.AzureException;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import javax.inject.Inject;
import java.util.Optional;

/** Implements the {@link PartnerMetadataStorage} using files stored in an Azure Storage Account. */
public class AzureStorageAccountPartnerMetadataStorage implements PartnerMetadataStorage {

    private static final AzureStorageAccountPartnerMetadataStorage INSTANCE =
            new AzureStorageAccountPartnerMetadataStorage();

    @Inject Formatter formatter;
    @Inject Logger logger;
    @Inject AzureClient client;

    private AzureStorageAccountPartnerMetadataStorage() {}

    public static AzureStorageAccountPartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public PartnerMetadata readMetadata(final String uniqueId) throws PartnerMetadataException {
        String metadataFileName = getMetadataFileName(uniqueId);
        logger.logInfo("Reading metadata for " + metadataFileName);
        try {
            BlobClient blobClient = client.getBlobClient(metadataFileName);
            logger.logInfo("Reading metadata from " + blobClient.getBlobUrl());
            String content = blobClient.downloadContent().toString();
            return formatter.convertJsonToObject(content, new TypeReference<>() {});
        } catch (AzureException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Failed to download metadata file " + metadataFileName, e);
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        String metadataFileName = getMetadataFileName(metadata.uniqueId());
        logger.logInfo("Saving metadata for " + metadataFileName);
        try {
            BlobClient blobClient = client.getBlobClient(metadataFileName);
            String content = formatter.convertToJsonString(metadata);
            blobClient.upload(BinaryData.fromString(content), true);
            logger.logInfo("Saved metadata to " + blobClient.getBlobUrl());
        } catch (AzureException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Failed to upload metadata file " + metadataFileName, e);
        }
    }

    public static String getMetadataFileName(String uniqueId) {
        return uniqueId + ".json";
    }
}
