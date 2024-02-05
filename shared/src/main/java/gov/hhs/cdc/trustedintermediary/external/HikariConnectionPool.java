package gov.hhs.cdc.trustedintermediary.external;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool;
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A shared connection pool for connecting to the database. The class currently spins up 10
 * connections on server startup. This is the default Hikari behavior. For troubleshooting or tuning
 * help please check the HikariCP repo for info on how to tune the connection performance should any
 * issues arise.
 */
public class HikariConnectionPool implements ConnectionPool {

    private static final HikariConnectionPool INSTANCE = new HikariConnectionPool();

    public final HikariDataSource ds;

    private HikariConnectionPool() {
        String user =
                ApplicationContext.getProperty("DB_USER") != null
                        ? ApplicationContext.getProperty("DB_USER")
                        : "";
        //        String pass =
        //                !ApplicationContext.getEnvironment().equalsIgnoreCase("local")
        //                        ? new DefaultAzureCredentialBuilder()
        //                                .build()
        //                                .getTokenSync(
        //                                        new TokenRequestContext()
        //                                                .addScopes(
        //
        // "https://ossrdbms-aad.database.windows.net/.default"))
        //                                .getToken()
        //                        : ApplicationContext.getProperty("DB_PASS");
        String pass =
                ApplicationContext.getImplementation(DatabaseCredentialsProvider.class)
                        .getPassword();
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

    public static HikariConnectionPool getInstance() {
        return INSTANCE;
    }
}
