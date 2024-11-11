package gov.hhs.cdc.trustedintermediary.rse2e;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class AzureBlobOrganizer {

    private final BlobContainerClient blobContainerClient;
    private final String azureStorageContainerName;

    public AzureBlobOrganizer(
            BlobContainerClient blobContainerClient, String azureStorageContainerName) {
        this.blobContainerClient = blobContainerClient;
        this.azureStorageContainerName = azureStorageContainerName;
    }

    public void organizeBlobsByDate() {
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            BlobClient sourceBlob = blobContainerClient.getBlobClient(blobItem.getName());
            BlobProperties properties = sourceBlob.getProperties();

            if (isInDateFolder(blobItem.getName())) {
                continue;
            }

            LocalDate blobDate =
                    properties.getLastModified().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            String newPath = createDateBasedPath(blobDate, blobItem.getName());
            BlobClient destinationBlob = blobContainerClient.getBlobClient(newPath);
            destinationBlob.beginCopy(sourceBlob.getBlobUrl(), null);
            sourceBlob.delete();
        }
    }

    private String createDateBasedPath(LocalDate date, String originalName) {
        return String.format(
                "%s/%d/%02d/%02d/%s",
                azureStorageContainerName,
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                originalName);
    }

    private boolean isInDateFolder(String blobName) {
        String[] parts = blobName.split("/");
        return parts.length >= 4
                && parts[0].equals(azureStorageContainerName)
                && parts[1].matches("\\d{4}")
                && parts[2].matches("\\d{2}")
                && parts[3].matches("\\d{2}");
    }
}
