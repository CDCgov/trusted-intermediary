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
import java.util.Optional;
import javax.inject.Inject;

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
    public Optional<PartnerMetadata> readMetadata(final String uniqueId)
            throws PartnerMetadataException {
        String metadataFileName = getMetadataFileName(uniqueId);
        try {
            BlobClient blobClient = client.getBlobClient(metadataFileName);
            String blobUrl = blobClient.getBlobUrl();
            logger.logInfo("Reading metadata from " + blobUrl);
            if (!blobClient.exists()) {
                logger.logWarning("Metadata blob not found: {}", blobUrl);
                return Optional.empty();
            }
            String content = blobClient.downloadContent().toString();
            PartnerMetadata metadata =
                    formatter.convertJsonToObject(content, new TypeReference<>() {});
            return Optional.ofNullable(metadata);
        } catch (AzureException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Failed to download metadata file " + metadataFileName, e);
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        String metadataFileName = getMetadataFileName(metadata.uniqueId());
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
