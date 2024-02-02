package gov.hhs.cdc.trustedintermediary.external.azure;

/**
 * AzureDatabaseCredentialsProvider is a class responsible for providing credentials for a database
 * deployed in Azure.
 */
public class AzureDatabaseCredentialsProvider {

    private static final AzureDatabaseCredentialsProvider INSTANCE =
            new AzureDatabaseCredentialsProvider();

    public static AzureDatabaseCredentialsProvider getInstance() {
        return INSTANCE;
    }

    private AzureDatabaseCredentialsProvider() {}
}
