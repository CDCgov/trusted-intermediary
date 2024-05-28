package gov.hhs.cdc.trustedintermediary.external;

import com.zaxxer.hikari.HikariDataSource;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A shared connection pool for connecting to the database. The class currently spins up 10
 * connections on server startup. This is the default Hikari behavior. For troubleshooting or tuning
 * help please check the HikariCP repo for info on how to tune the connection performance should any
 * issues arise.
 */
public class HikariConnectionPool implements ConnectionPool {

    private static HikariConnectionPool INSTANCE;

    private final HikariDataSource ds;

    private static final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);

    private HikariConnectionPool() {
        ds = constructHikariDataSource();
    }

    public static synchronized HikariConnectionPool getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HikariConnectionPool();
        }
        return INSTANCE;
    }

    static HikariDataSource constructHikariDataSource() {

        HikariDataSource config = new PasswordChangingHikariDataSource();

        try {
            String maxLife = ApplicationContext.getProperty("DB_MAX_LIFETIME");
            if (maxLife != null && !maxLife.isEmpty()) {
                config.setMaxLifetime(Long.parseLong(maxLife));
            } else {
                LOGGER.logInfo("Using Hikari default DB Max Lifetime");
            }
        } catch (NumberFormatException e) {
            LOGGER.logInfo("Using Hikari default DB Max Lifetime");
        }

        String user = ApplicationContext.getProperty("DB_USER", "");
        String serverName = ApplicationContext.getProperty("DB_URL", "");
        String dbName = ApplicationContext.getProperty("DB_NAME", "");
        String dbPort = ApplicationContext.getProperty("DB_PORT", "");

        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.setUsername(user);
        config.addDataSourceProperty("serverName", serverName);
        config.addDataSourceProperty("databaseName", dbName);
        config.addDataSourceProperty("portNumber", dbPort);

        return config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
