package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
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

    private PostgresDao() {}

    protected Connection connect() throws SQLException {
        Connection conn;
        String url =
                "jdbc:postgresql://"
                        + ApplicationContext.getProperty("DB_URL")
                        + ":"
                        + ApplicationContext.getProperty("DB_PORT")
                        + "/"
                        + ApplicationContext.getProperty("DB_NAME");

        Properties props = new Properties();
        props.setProperty("user", ApplicationContext.getProperty("DB_USER"));
        props.setProperty("password", ApplicationContext.getProperty("DB_PASS"));

        // TODO: Change this based on env
        props.setProperty("ssl", ApplicationContext.getProperty("DB_SSL"));
        conn = driverManager.getConnection(url, props);
        logger.logInfo("DB Connected Successfully");
        return conn;
    }

    public static PostgresDao getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized void upsertMetadata(
            String id, String sender, String receiver, String hash, Instant timeReceived)
            throws SQLException {

        try (Connection conn = connect();
                PreparedStatement statement =
                        conn.prepareStatement("INSERT INTO metadata VALUES (?, ?, ?, ?, ?)")) {
            // TODO: Update the below statement to handle on conflict, after we figure out what that
            // behavior should be
            statement.setString(1, id);
            statement.setString(2, sender);
            statement.setString(3, receiver);
            statement.setString(4, hash);
            statement.setTimestamp(5, Timestamp.from(timeReceived));

            int result = statement.executeUpdate();
            // TODO: Do something if our update returns 0...
            logger.logInfo(String.valueOf(result));

        } catch (Exception e) {
            logger.logError("Error updating data: " + e.getMessage());
            throw new SQLException();
        }
    }
}
