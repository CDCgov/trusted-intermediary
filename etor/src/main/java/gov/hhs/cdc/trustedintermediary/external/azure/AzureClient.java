package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.storage.blob.BlobClient;

public interface AzureClient {
    BlobClient getBlobClient(String blobName);
}
