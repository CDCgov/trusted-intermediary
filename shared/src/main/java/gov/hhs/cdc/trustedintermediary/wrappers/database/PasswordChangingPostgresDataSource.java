package gov.hhs.cdc.trustedintermediary.wrappers.database;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * This class extends {@link PGSimpleDataSource} and overrides the getConnection methods to ensure
 * that the latest password is always used when establishing a connection to the database. It
 * retrieves the latest password from the {@link DatabaseCredentialsProvider}. This class is
 * referenced through a string elsewhere, even though it seems it isn't referenced.
 */
public class PasswordChangingPostgresDataSource extends PGSimpleDataSource {
    @Override
    public Connection getConnection() throws SQLException {
        ApplicationContext.getImplementation(Logger.class)
                .logInfo("Establishing new connection to the database");

        var latestPassword =
                ApplicationContext.getImplementation(DatabaseCredentialsProvider.class)
                        .getPassword();
        this.setPassword(latestPassword);

        return super.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        ApplicationContext.getImplementation(Logger.class)
                .logInfo("Establishing new connection to the database with a username");

        var latestPassword =
                ApplicationContext.getImplementation(DatabaseCredentialsProvider.class)
                        .getPassword();
        this.setPassword(latestPassword);

        return super.getConnection(username, latestPassword);
    }
}
