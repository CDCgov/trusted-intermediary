package gov.hhs.cdc.trustedintermediary.external;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.ConnectionPool;
import java.sql.Connection;
import java.sql.SQLException;

public class EtorConnectionPool implements ConnectionPool {

    private static final EtorConnectionPool INSTANCE = new EtorConnectionPool();

    private EtorConnectionPool() {}

    private static HikariConfig config = new HikariDataSource();
    private static HikariDataSource ds;

    static {
        String user =
                ApplicationContext.getProperty("DB_USER") != null
                        ? ApplicationContext.getProperty("DB_USER")
                        : "";
        // TODO: Need to update DB_PASS for Azure
        String pass =
                ApplicationContext.getProperty("DB_PASS") != null
                        ? ApplicationContext.getProperty("DB_PASS")
                        : "";
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
