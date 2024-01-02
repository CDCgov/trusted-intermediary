package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Wrapper class for SqlDriverManager */
public class EtorSqlDriverManager implements SqlDriverManager {

    private static final EtorSqlDriverManager INSTANCE = new EtorSqlDriverManager();

    private EtorSqlDriverManager() {}

    @Override
    public Connection getConnection(String url, Properties props) throws SQLException {
        return DriverManager.getConnection(url, props);
    }

    public static EtorSqlDriverManager getInstance() {
        return INSTANCE;
    }
}
