package gov.hhs.cdc.trustedintermediary.external.azure;

import gov.hhs.cdc.trustedintermediary.external.database.DatabaseCredentialsProvider;
import javax.inject.Inject;

/**
 * AzureDatabaseCredentialsProvider is a class responsible for providing credentials for a database
 * deployed in Azure.
 */
public class AzureDatabaseCredentialsProvider implements DatabaseCredentialsProvider {

    private static final AzureDatabaseCredentialsProvider INSTANCE =
            new AzureDatabaseCredentialsProvider();

    @Inject AzureClient azureClient;

    public static AzureDatabaseCredentialsProvider getInstance() {
        return INSTANCE;
    }

    private AzureDatabaseCredentialsProvider() {}

    @Override
    public String getPassword() {
        return azureClient.getScopedToken("https://ossrdbms-aad.database.windows.net/.default");
    }
}
