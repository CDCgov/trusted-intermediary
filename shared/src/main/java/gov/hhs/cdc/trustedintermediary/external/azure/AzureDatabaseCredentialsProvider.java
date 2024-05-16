package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider;
import javax.inject.Inject;

/**
 * AzureDatabaseCredentialsProvider is a class responsible for providing credentials for a database
 * deployed in Azure.
 */
public class AzureDatabaseCredentialsProvider implements DatabaseCredentialsProvider {

    private static final AzureDatabaseCredentialsProvider INSTANCE =
            new AzureDatabaseCredentialsProvider();

    public static AzureDatabaseCredentialsProvider getInstance() {
        return INSTANCE;
    }

    private AzureDatabaseCredentialsProvider() {}

    @Inject Logger logger;

    @Override
    public String getPassword() {

        logger.logInfo("Fetching credentials from Azure");

        return new DefaultAzureCredentialBuilder()
                .build()
                .getTokenSync(
                        new TokenRequestContext()
                                .addScopes("https://ossrdbms-aad.database.windows.net/.default"))
                .getToken();
    }
}
