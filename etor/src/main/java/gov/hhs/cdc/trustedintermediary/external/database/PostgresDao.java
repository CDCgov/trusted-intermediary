package gov.hhs.cdc.trustedintermediary.external.database;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.wrappers.DbDao;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        logger.logInfo("going to connect to db url {}", url);

        // Ternaries prevent NullPointerException during testing since we decided not to mock env
        // vars.
        var user =
                ApplicationContext.getProperty("DB_USER") == null
                        ? ""
                        : ApplicationContext.getProperty("DB_USER");
        //        var pass =
        //                ApplicationContext.getProperty("DB_PASS") == null
        //                        ? ""
        //                        : ApplicationContext.getProperty("DB_PASS");
        var ssl =
                ApplicationContext.getProperty("DB_SSL") == null
                        ? ""
                        : ApplicationContext.getProperty("DB_SSL");

        Properties props = new Properties();
        props.setProperty("user", user);
        logger.logInfo("About to get the db password");
        String token =
                new DefaultAzureCredentialBuilder()
                        .build()
                        .getTokenSync(
                                new TokenRequestContext()
                                        .addScopes(
                                                "https://ossrdbms-aad.database.windows.net/.default")) // TODO: This string could need to be "https://ossrdbms-aad.database.windows.net/.default"
                        .getToken();

        logger.logInfo("got the db password");

        props.setProperty("password", token);

        props.setProperty("ssl", ssl);
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

    @Override
    public synchronized PartnerMetadata fetchMetadata(String uniqueId) throws SQLException {
        try (Connection conn = connect();
                PreparedStatement statement =
                        conn.prepareStatement("SELECT * FROM metadata where message_id = ?")) {

            statement.setString(1, uniqueId);

            ResultSet result = statement.executeQuery();

            result.next();
            return new PartnerMetadata(
                    result.getString("message_id"),
                    result.getString("sender"),
                    result.getString("receiver"),
                    result.getTimestamp("time_received").toInstant(),
                    result.getString("hash_of_order"));

        } catch (SQLException e) {
            logger.logError("Error fetching data: " + e.getMessage());
            throw new SQLException();
        }
    }
}
