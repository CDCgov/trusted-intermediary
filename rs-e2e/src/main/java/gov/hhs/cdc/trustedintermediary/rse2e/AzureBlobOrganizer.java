package gov.hhs.cdc.trustedintermediary.rse2e;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class AzureBlobOrganizer {

    private final BlobContainerClient blobContainerClient;

    public AzureBlobOrganizer(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    public void organizeAndCleanupBlobsByDate(int retentionDays) {
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());

            BlobProperties properties = blobClient.getProperties();
            LocalDate blobDate =
                    properties.getLastModified().toInstant().atZone(ZoneOffset.UTC).toLocalDate();

            LocalDate retentionDate = LocalDate.now().minusDays(retentionDays);
            if (blobDate.isBefore(retentionDate)) {
                blobClient.delete();
                continue;
            }

            if (isInDateFolder(blobItem.getName())) {
                continue;
            }

            String newPath = createDateBasedPath(blobDate, blobClient.getBlobName());
            BlobClient destinationBlob = blobContainerClient.getBlobClient(newPath);

            destinationBlob.beginCopy(blobClient.getBlobUrl(), null);
            blobClient.delete();
        }
    }

    private String createDateBasedPath(LocalDate date, String originalName) {
        return String.format(
                "%d/%02d/%02d/%s",
                date.getYear(), date.getMonthValue(), date.getDayOfMonth(), originalName);
    }

    private boolean isInDateFolder(String blobName) {
        String[] parts = blobName.split("/");
        return parts.length >= 4
                && parts[1].matches("\\d{4}")
                && parts[2].matches("\\d{2}")
                && parts[3].matches("\\d{2}");
    }
}
