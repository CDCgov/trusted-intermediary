package gov.hhs.cdc.trustedintermediary.external.database;

import com.azure.core.annotation.Post;
import gov.hhs.cdc.trustedintermediary.wrappers.DbConnection;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class PostgresConnection implements DbConnection {

    @Inject
    Logger logger;
    private static final PostgresConnection INSTANCE = new PostgresConnection();

    private PostgresConnection() {

        String url = "jdbc:postgressql://localhost:5433/intermediary";

        Properties props = new Properties();
        props.setProperty("user", "intermediary");
        props.setProperty("password", "changeIT!");
        props.setProperty("ssl", "true");
        try {
            Connection conn = DriverManager.getConnection(url, props);
        }
        catch(Exception e){
            //TODO: More specific exception for the different potential errors
            logger.logError(e.getMessage());
        }
    }

    public static PostgresConnection connect() throws SQLException {


    return INSTANCE;
    }


}
