package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider;

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

    @Override
    public String getPassword() {

        // this method is at least called during bootstrapping, so we can't use @Inject
        ApplicationContext.getImplementation(Logger.class)
                .logInfo("Fetching credentials from Azure");

        return new DefaultAzureCredentialBuilder()
                .build()
                .getTokenSync(
                        new TokenRequestContext()
                                .addScopes("https://ossrdbms-aad.database.windows.net/.default"))
                .getToken();
    }
}
