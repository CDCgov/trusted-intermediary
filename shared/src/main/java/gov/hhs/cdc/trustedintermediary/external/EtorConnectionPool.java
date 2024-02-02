package gov.hhs.cdc.trustedintermediary.external;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool;
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Inject;

public class EtorConnectionPool implements ConnectionPool {

    private static final EtorConnectionPool INSTANCE = new EtorConnectionPool();

    @Inject DatabaseCredentialsProvider credentialsProvider;
    private final HikariDataSource ds;

    private EtorConnectionPool() {
        String user =
                ApplicationContext.getProperty("DB_USER") != null
                        ? ApplicationContext.getProperty("DB_USER")
                        : "";
        String pass = credentialsProvider.getPassword();
        String serverName =
                ApplicationContext.getProperty("DB_URL") != null
                        ? ApplicationContext.getProperty("DB_URL")
                        : "";
        String dbName =
                ApplicationContext.getProperty("DB_NAME") != null
                        ? ApplicationContext.getProperty("DB_NAME")
                        : "";
        String dbPort =
                ApplicationContext.getProperty("DB_PORT") != null
                        ? ApplicationContext.getProperty("DB_PORT")
                        : "";

        HikariConfig config = new HikariDataSource();
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.addDataSourceProperty("user", user);
        config.addDataSourceProperty("password", pass);
        config.addDataSourceProperty("serverName", serverName);
        config.addDataSourceProperty("databaseName", dbName);
        config.addDataSourceProperty("portNumber", dbPort);
        // TODO: Add tests
        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static EtorConnectionPool getInstance() {
        return INSTANCE;
    }
}
