package gov.hhs.cdc.trustedintermediary.wrappers.database;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.codec.digest.DigestUtils;
import org.postgresql.ds.PGSimpleDataSource;

public class PasswordChangingPostgresDataSource extends PGSimpleDataSource {
    @Override
    public Connection getConnection() throws SQLException {
        ApplicationContext.getImplementation(Logger.class)
                .logInfo("Establishing new connection to the database");

        var latestPassword =
                ApplicationContext.getImplementation(DatabaseCredentialsProvider.class)
                        .getPassword();

        var passwordHash = DigestUtils.sha256Hex(latestPassword);
        ApplicationContext.getImplementation(Logger.class)
                .logInfo("Password hash={}", passwordHash);

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

        var passwordHash = DigestUtils.sha256Hex(latestPassword);
        ApplicationContext.getImplementation(Logger.class)
                .logInfo("Password hash={}", passwordHash);

        this.setPassword(latestPassword);

        return super.getConnection(username, latestPassword);
    }
}
