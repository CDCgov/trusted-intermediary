package gov.hhs.cdc.trustedintermediary.external;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import java.sql.Connection;
import java.sql.SQLException;

public class EtorConnectionPool {

    private static final EtorConnectionPool INSTANCE = new EtorConnectionPool();

    private EtorConnectionPool() {}

    private static HikariConfig config = new HikariDataSource();
    private static HikariDataSource ds;

    static {
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.addDataSourceProperty("user", ApplicationContext.getProperty("DB_USER"));
        config.addDataSourceProperty("password", ApplicationContext.getProperty("DB_PASS"));
        config.addDataSourceProperty("serverName", "localhost");
        config.addDataSourceProperty("databaseName", "intermediary");
        config.addDataSourceProperty("portNumber", 5433);

        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static EtorConnectionPool getInstance() {
        return INSTANCE;
    }
}
