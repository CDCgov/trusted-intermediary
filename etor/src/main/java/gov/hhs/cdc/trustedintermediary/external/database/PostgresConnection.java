package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.wrappers.DbConnection;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.inject.Inject;

public class PostgresConnection implements DbConnection {

    @Inject Logger logger;
    private static final PostgresConnection INSTANCE = new PostgresConnection();
    private Connection conn;

    private PostgresConnection() {}

    @Override
    public void connect() {
        String url = "jdbc:postgresql://localhost:5433/intermediary";

        Properties props = new Properties();
        props.setProperty("user", "intermediary");
        props.setProperty("password", "changeIT!");
        props.setProperty("ssl", "false");
        try {
            conn = DriverManager.getConnection(url, props);
            logger.logInfo("DB Connected Successfully");
        } catch (Exception e) {
            // TODO: More specific exception for the different potential errors?
            logger.logError(e.getMessage());
        }
    }

    public static PostgresConnection getInstance() {
        return INSTANCE;
    }

    public synchronized Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                connect();
            }
            return conn;
        } catch (SQLException e) {
            logger.logError("Error getting connection: " + e.getMessage());
            return null;
        }
    }

    public void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                logger.logInfo("DB Connection Closed Successfully");
            } catch (SQLException e) {
                logger.logError(("Error closing connection: " + e.getMessage()));
            }
        }
    }
}
