package gov.hhs.cdc.trustedintermediary.external.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * ResultSetIterator iterates over a SQL {@link ResultSet}. This helps stream over a {@link
 * ResultSet}.
 */
public class ResultSetIterator implements Iterator<ResultSet> {
    private final ResultSet resultSet;

    public ResultSetIterator(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public boolean hasNext() {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResultSet next() {
        return resultSet;
    }
}
