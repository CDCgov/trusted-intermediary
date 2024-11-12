package gov.hhs.cdc.trustedintermediary.rse2e;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.options.BlobBeginCopyOptions;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

public class AzureBlobOrganizer {

    private final BlobContainerClient blobContainerClient;

    private static final ZoneId TIME_ZONE = ZoneOffset.UTC;

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);

    public AzureBlobOrganizer(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    public void organizeAndCleanupBlobsByDate(int retentionDays) {
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            String sourcePath = blobItem.getName();
            try {
                BlobClient sourceBlob = blobContainerClient.getBlobClient(sourcePath);
                BlobProperties sourceProperties = sourceBlob.getProperties();
                LocalDate sourceCreationDate =
                        sourceProperties
                                .getCreationTime()
                                .toInstant()
                                .atZone(TIME_ZONE)
                                .toLocalDate();

                LocalDate retentionDate = LocalDate.now(TIME_ZONE).minusDays(retentionDays);
                if (sourceCreationDate.isBefore(retentionDate)) {
                    sourceBlob.delete();
                    logger.logInfo("Deleted old blob: {}", sourcePath);
                    continue;
                }

                if (isInDateFolder(sourcePath, sourceCreationDate)) {
                    continue;
                }

                String sourceUrl = sourceBlob.getBlobUrl();
                Map<String, String> sourceMetadata = sourceProperties.getMetadata();
                BlobBeginCopyOptions copyOptions =
                        new BlobBeginCopyOptions(sourceUrl).setMetadata(sourceMetadata);

                String destinationPath = createDateBasedPath(sourceCreationDate, sourcePath);
                BlobClient destinationBlob = blobContainerClient.getBlobClient(destinationPath);
                destinationBlob.beginCopy(copyOptions).waitForCompletion(Duration.ofSeconds(30));

                if (sourceBlob.getProperties().getBlobSize()
                        == destinationBlob.getProperties().getBlobSize()) {
                    sourceBlob.delete();
                    logger.logInfo("Moved blob {} to {}", sourcePath, destinationPath);
                } else {
                    destinationBlob.delete();
                    logger.logError("Failed to copy blob: " + sourcePath);
                }
            } catch (Exception e) {
                logger.logError("Error processing blob: " + sourcePath, e);
            }
        }
    }

    private String createDateBasedPath(LocalDate date, String originalName) {
        return String.format(
                "%d/%02d/%02d/%s",
                date.getYear(), date.getMonthValue(), date.getDayOfMonth(), originalName);
    }

    private boolean isInDateFolder(String blobPath, LocalDate creationDate) {
        String expectedPath =
                String.format(
                        "%d/%02d/%02d/",
                        creationDate.getYear(),
                        creationDate.getMonthValue(),
                        creationDate.getDayOfMonth());
        return blobPath.startsWith(expectedPath);
    }
}
