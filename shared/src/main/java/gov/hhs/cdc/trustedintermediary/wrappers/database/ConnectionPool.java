package gov.hhs.cdc.trustedintermediary.wrappers.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPool {

    Connection getConnection() throws SQLException;
}
