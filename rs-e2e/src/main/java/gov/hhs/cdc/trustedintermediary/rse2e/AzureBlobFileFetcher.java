package gov.hhs.cdc.trustedintermediary.rse2e;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * The AzureBlobFileFetcher class implements the {@link FileFetcher FileFetcher} interface and
 * fetches files from an Azure Blob Storage container.
 */
public class AzureBlobFileFetcher implements FileFetcher {

    // We're using UTC for now, but we plan to change the timezone to be more realistic to the
    // working timezones in our teams
    private static final ZoneId TIME_ZONE = ZoneOffset.UTC;
    private static final int RETENTION_DAYS = 90;
    private static final String CONTAINER_NAME = "automated";

    private final BlobContainerClient blobContainerClient;

    private static final FileFetcher INSTANCE = new AzureBlobFileFetcher();

    private AzureBlobFileFetcher() {
        String azureStorageConnectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

        if (azureStorageConnectionString == null || azureStorageConnectionString.isEmpty()) {
            throw new IllegalArgumentException(
                    "Environment variable AZURE_STORAGE_CONNECTION_STRING is not set");
        }
        this.blobContainerClient =
                new BlobContainerClientBuilder()
                        .connectionString(azureStorageConnectionString)
                        .containerName(CONTAINER_NAME)
                        .buildClient();

        AzureBlobOrganizer blobOrganizer = new AzureBlobOrganizer(blobContainerClient);
        blobOrganizer.organizeAndCleanupBlobsByDate(RETENTION_DAYS, TIME_ZONE);
    }

    public static FileFetcher getInstance() {
        return INSTANCE;
    }

    @Override
    public List<HL7FileStream> fetchFiles() {
        List<HL7FileStream> recentFiles = new ArrayList<>();
        LocalDate mostRecentDay = null;

        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
            BlobProperties properties = blobClient.getProperties();

            LocalDate blobCreationDate =
                    properties.getLastModified().toInstant().atZone(TIME_ZONE).toLocalDate();

            if (mostRecentDay != null && blobCreationDate.isBefore(mostRecentDay)) {
                continue;
            }

            if (mostRecentDay == null || blobCreationDate.isAfter(mostRecentDay)) {
                mostRecentDay = blobCreationDate;
                recentFiles.clear();
            }

            recentFiles.add(
                    new HL7FileStream(blobClient.getBlobName(), blobClient.openInputStream()));
        }

        return recentFiles;
    }
}
