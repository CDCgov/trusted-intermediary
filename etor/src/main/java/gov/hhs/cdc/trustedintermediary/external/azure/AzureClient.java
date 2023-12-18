package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;

/** This class represents a client for interacting with Azure Blob Storage. */
public class AzureClient {

    private static final String STORAGE_ACCOUNT_BLOB_ENDPOINT =
            ApplicationContext.getProperty("STORAGE_ACCOUNT_BLOB_ENDPOINT");
    private static final String METADATA_CONTAINER_NAME =
            ApplicationContext.getProperty("METADATA_CONTAINER_NAME");

    private static final AzureClient INSTANCE = new AzureClient();

    private static BlobContainerClient BLOB_CONTAINER_CLIENT;

    private AzureClient() {}

    public static AzureClient getInstance() {

        /*
        BLOB_CONTAINER_CLIENT is initialized here inside the getInstance method instead of the static context above
        to ensure that it is not created until it is needed.  This prevents an exception being thrown in the unit
        test context where `STORAGE_ACCOUNT_BLOB_ENDPOINT` is empty.
         */
        BLOB_CONTAINER_CLIENT =
                new BlobServiceClientBuilder()
                        .endpoint(STORAGE_ACCOUNT_BLOB_ENDPOINT)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient()
                        .getBlobContainerClient(METADATA_CONTAINER_NAME);

        return INSTANCE;
    }

    public BlobClient getBlobClient(String blobName) {
        return BLOB_CONTAINER_CLIENT.getBlobClient(blobName);
    }

    public String getScopedToken(String scope) {
        return new DefaultAzureCredentialBuilder()
                .build()
                .getTokenSync(new TokenRequestContext().addScopes(scope))
                .getToken();
    }
}
