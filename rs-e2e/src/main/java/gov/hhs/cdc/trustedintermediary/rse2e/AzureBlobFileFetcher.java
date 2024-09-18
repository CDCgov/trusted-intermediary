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

public class AzureBlobFileFetcher implements FileFetcher {
    private static final String AZURE_STORAGE_CONNECTION_STRING =
            System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final String AZURE_STORAGE_CONTAINER_NAME = "automated";

    private final BlobContainerClient blobContainerClient;

    public AzureBlobFileFetcher() {
        if (AZURE_STORAGE_CONNECTION_STRING == null || AZURE_STORAGE_CONNECTION_STRING.isEmpty()) {
            throw new IllegalArgumentException(
                    "Environment variable AZURE_STORAGE_CONNECTION_STRING is not set");
        }
        this.blobContainerClient =
                new BlobContainerClientBuilder()
                        .connectionString(AZURE_STORAGE_CONNECTION_STRING)
                        .containerName(AZURE_STORAGE_CONTAINER_NAME)
                        .buildClient();
    }

    @Override
    public List<HL7FileStream> fetchFiles() {
        List<HL7FileStream> recentFiles = new ArrayList<>();
        LocalDate mostRecentDay = null;

        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
            BlobProperties properties = blobClient.getProperties();
            LocalDate blobCreationDate =
                    properties.getLastModified().toInstant().atZone(ZoneOffset.UTC).toLocalDate();

            if (mostRecentDay == null || blobCreationDate.isAfter(mostRecentDay)) {
                mostRecentDay = blobCreationDate;
                recentFiles.clear();
                recentFiles.add(
                        new HL7FileStream(blobClient.getBlobName(), blobClient.openInputStream()));
            } else if (blobCreationDate.equals(mostRecentDay)) {
                recentFiles.add(
                        new HL7FileStream(blobClient.getBlobName(), blobClient.openInputStream()));
            }
        }

        return recentFiles;
    }
}
