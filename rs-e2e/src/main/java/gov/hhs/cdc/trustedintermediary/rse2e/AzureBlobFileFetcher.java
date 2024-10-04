package gov.hhs.cdc.trustedintermediary.rse2e;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * The AzureBlobFileFetcher class implements the {@link FileFetcher FileFetcher} interface and
 * fetches files from an Azure Blob Storage container.
 */
public class AzureBlobFileFetcher implements FileFetcher {

    private static final FileFetcher INSTANCE = new AzureBlobFileFetcher();

    private final BlobContainerClient blobContainerClient;

    private AzureBlobFileFetcher() {
        String azureStorageConnectionName = "automated";
        String azureStorageConnectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

        if (azureStorageConnectionString == null || azureStorageConnectionString.isEmpty()) {
            throw new IllegalArgumentException(
                    "Environment variable AZURE_STORAGE_CONNECTION_STRING is not set");
        }
        this.blobContainerClient =
                new BlobContainerClientBuilder()
                        .connectionString(azureStorageConnectionString)
                        .containerName(azureStorageConnectionName)
                        .buildClient();
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

            // Currently we're doing everything in UTC. If we start uploading files manually and
            // running
            // this test manually, we may want to revisit this logic and/or the file structure
            // because midnight UTC is 5pm PDT on the previous day
            LocalDate blobCreationDate =
                    properties.getLastModified().toInstant().atZone(ZoneOffset.UTC).toLocalDate();

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
