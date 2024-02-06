package gov.hhs.cdc.trustedintermediary.wrappers.database;

import java.sql.Connection;
import java.sql.SQLException;

/** Wrapper interface for connection pool libraries. */
public interface ConnectionPool {

    Connection getConnection() throws SQLException;
}
