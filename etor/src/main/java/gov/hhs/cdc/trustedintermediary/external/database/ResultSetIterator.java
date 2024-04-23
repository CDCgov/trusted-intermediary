package gov.hhs.cdc.trustedintermediary.external.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * ResultSetIterator iterates over a SQL {@link ResultSet}. This helps stream over a {@link
 * ResultSet}.
 */
public class ResultSetIterator implements Iterator<ResultSet> {
    private final ResultSet resultSet;
    private boolean hasNext;

    public ResultSetIterator(ResultSet resultSet) {
        this.resultSet = resultSet;
        hasNext = true;
    }

    @Override
    public boolean hasNext() {
        try {
            hasNext = resultSet.next();
        } catch (SQLException e) {
            hasNext = false;
            throw new RuntimeException(e);
        }

        return hasNext;
    }

    @Override
    public ResultSet next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }

        return resultSet;
    }
}
