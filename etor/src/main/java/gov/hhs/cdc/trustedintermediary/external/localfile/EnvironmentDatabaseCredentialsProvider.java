package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.external.database.DatabaseCredentialsProvider;

public class EnvironmentDatabaseCredentialsProvider implements DatabaseCredentialsProvider {

    private static final EnvironmentDatabaseCredentialsProvider INSTANCE =
            new EnvironmentDatabaseCredentialsProvider();

    public static EnvironmentDatabaseCredentialsProvider getInstance() {
        return INSTANCE;
    }

    private EnvironmentDatabaseCredentialsProvider() {}

    @Override
    public String getPassword() {
        return ApplicationContext.getProperty("DB_PASS");
    }
}
