package gov.hhs.cdc.trustedintermediary.wrappers;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPool {

    Connection getConnection() throws SQLException;
}
