package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.wrappers.DbDao;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import javax.inject.Inject;

public class PostgresDao implements DbDao {

    @Inject Logger logger;
    @Inject SqlDriverManager driverManager;
    private static final PostgresDao INSTANCE = new PostgresDao();
    private Connection conn;

    private PostgresDao() {}

    protected void connect() throws SQLException {
        String url = "jdbc:postgresql://localhost:5433/intermediary";

        Properties props = new Properties();
        props.setProperty("user", "intermediary");
        props.setProperty("password", "changeIT!");

        // TODO: Change this based on env
        props.setProperty("ssl", "false");
        conn = driverManager.getConnection(url, props);
        logger.logInfo("DB Connected Successfully");
    }

    public static PostgresDao getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                connect();
            }
            return conn;
        } catch (SQLException e) {
            logger.logError("Error getting connection: " + e.getMessage());
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public void upsertMetadata(
            String id, String sender, String receiver, String hash, Instant timeReceived) {
        try {
            // TODO: Update the below statement to handle on conflict, after we figure out what that
            // behavior should be
            PreparedStatement statement =
                    getConnection().prepareStatement("INSERT INTO metadata VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, sender);
            statement.setString(3, receiver);
            statement.setString(4, hash);
            statement.setTimestamp(5, Timestamp.from(timeReceived));

            int result = statement.executeUpdate();
            // TODO: Do something if our update returns 0...
            logger.logInfo(String.valueOf(result));
            statement.close();

        } catch (Exception e) {
            logger.logError("Error updating data: " + e.getMessage());
        } finally {
            closeConnection();
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
