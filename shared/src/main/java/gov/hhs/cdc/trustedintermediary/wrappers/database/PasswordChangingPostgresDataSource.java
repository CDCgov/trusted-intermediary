package gov.hhs.cdc.trustedintermediary.wrappers.database;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.ds.PGSimpleDataSource;

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
