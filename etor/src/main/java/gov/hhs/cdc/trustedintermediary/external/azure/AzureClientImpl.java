package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;

public class AzureClientImpl implements AzureClient {

    private static final String STORAGE_ACCOUNT_BLOB_ENDPOINT =
            ApplicationContext.getProperty("STORAGE_ACCOUNT_BLOB_ENDPOINT");
    private static final String METADATA_CONTAINER_NAME =
            ApplicationContext.getProperty("METADATA_CONTAINER_NAME");

    private static final AzureClientImpl INSTANCE = new AzureClientImpl();

    private static final BlobContainerClient BLOB_CONTAINER_CLIENT =
            new BlobServiceClientBuilder()
                    .endpoint(STORAGE_ACCOUNT_BLOB_ENDPOINT)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient()
                    .getBlobContainerClient(METADATA_CONTAINER_NAME);

    private AzureClientImpl() {}

    public static AzureClientImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public BlobClient getBlobClient(String blobName) {
        return BLOB_CONTAINER_CLIENT.getBlobClient(blobName);
    }
}
