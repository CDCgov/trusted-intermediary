package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider;

/**
 * The EnvironmentDatabaseCredentialsProvider class is an implementation of the
 * DatabaseCredentialsProvider interface. It retrieves the database credentials from environment
 * variables.
 */
public class EnvironmentDatabaseCredentialsProvider implements DatabaseCredentialsProvider {

    private static final EnvironmentDatabaseCredentialsProvider INSTANCE =
            new EnvironmentDatabaseCredentialsProvider();

    public static EnvironmentDatabaseCredentialsProvider getInstance() {
        return INSTANCE;
    }

    private EnvironmentDatabaseCredentialsProvider() {}

    @Override
    public String getPassword() {
        // this method is at least called during bootstrapping, so we can't use @Inject
        ApplicationContext.getImplementation(Logger.class)
                .logInfo("Fetching database credentials from environment variable DB_PASS");

        return ApplicationContext.getProperty("DB_PASS");
    }
}
