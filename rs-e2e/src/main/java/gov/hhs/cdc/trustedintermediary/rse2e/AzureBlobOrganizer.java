package gov.hhs.cdc.trustedintermediary.rse2e;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.CopyStatusType;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

/* AzureBlobOrganizer is responsible for organizing and cleaning up blobs in an Azure container */
public class AzureBlobOrganizer {

    private final BlobContainerClient blobContainerClient;

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);

    public AzureBlobOrganizer(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    public void organizeAndCleanupBlobsByDate(int retentionDays, ZoneId timeZone) {
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            String sourceName = blobItem.getName();
            try {
                BlobClient sourceBlob = blobContainerClient.getBlobClient(sourceName);
                BlobProperties sourceProperties = sourceBlob.getProperties();
                LocalDate sourceCreationDate =
                        sourceProperties
                                .getCreationTime()
                                .toInstant()
                                .atZone(timeZone)
                                .toLocalDate();

                LocalDate retentionDate = LocalDate.now(timeZone).minusDays(retentionDays);
                if (sourceCreationDate.isBefore(retentionDate)) {
                    sourceBlob.delete();
                    logger.logInfo("Deleted old blob: {}", sourceName);
                    continue;
                }

                if (AzureBlobHelper.isInDateFolder(sourceName, sourceCreationDate)) {
                    continue;
                }

                // TODO - separate the content by golden or automated so it can be distinguished
                // when its pulled down
                //  modify destinationName to be test folder specific
                //  possibly read blob and modify parseAndMapMessageByControlId or use its parts to
                // get a different MSH header like getIdentifier
                // possibly use a different receiver and filter on that

                String destinationName =
                        AzureBlobHelper.createDateBasedPath(sourceCreationDate, sourceName);

                BlobClient destinationBlob = blobContainerClient.getBlobClient(destinationName);
                destinationBlob
                        .beginCopy(sourceBlob.getBlobUrl(), null)
                        .waitForCompletion(Duration.ofSeconds(30));
                CopyStatusType copyStatus = destinationBlob.getProperties().getCopyStatus();
                if (copyStatus == CopyStatusType.SUCCESS) {
                    sourceBlob.delete();
                    logger.logInfo("Moved blob {} to {}", sourceName, destinationName);
                } else {
                    destinationBlob.delete();
                    logger.logError("Failed to copy blob: " + sourceName);
                }
            } catch (Exception e) {
                logger.logError("Error processing blob: " + sourceName, e);
            }
        }
    }
}
