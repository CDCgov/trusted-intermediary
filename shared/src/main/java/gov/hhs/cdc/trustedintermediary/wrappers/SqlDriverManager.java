package gov.hhs.cdc.trustedintermediary.wrappers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public interface SqlDriverManager {

    Connection getConnection(String url, Properties props) throws SQLException;
}
