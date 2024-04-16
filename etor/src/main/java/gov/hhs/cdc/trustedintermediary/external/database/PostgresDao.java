package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.inject.Inject;

/** Class for accessing and managing data for the postgres Database */
public class PostgresDao implements DbDao {

    private static final PostgresDao INSTANCE = new PostgresDao();

    @Inject ConnectionPool connectionPool;

    @Inject Formatter formatter;

    private PostgresDao() {}

    public static PostgresDao getInstance() {
        return INSTANCE;
    }

    @Override
    public void upsertData(String tableName, List<DbColumn> values, String conflictTarget)
            throws SQLException {
        // example SQL statement generated here:
        // INSERT INTO metadata_table (column_one, column_three, column_two, column_four)
        // VALUES (?, ?, ?, ?)
        // ON CONFLICT (column_one) DO UPDATE SET column_three = EXCLUDED.column_three, column_two =
        // EXCLUDED.column_two

        StringBuilder sqlStatementBuilder =
                new StringBuilder("INSERT INTO ").append(tableName).append(" (");

        values.forEach(dbColumn -> sqlStatementBuilder.append(dbColumn.name()).append(", "));
        removeLastTwoCharacters(sqlStatementBuilder); // remove the last unused ", "

        sqlStatementBuilder.append(") VALUES (");

        sqlStatementBuilder.append("?, ".repeat(values.size()));
        removeLastTwoCharacters(sqlStatementBuilder); // remove the last unused ", "
        sqlStatementBuilder.append(")");

        if (conflictTarget != null) {
            sqlStatementBuilder.append(" ON CONFLICT ").append(conflictTarget);

            boolean overwriteOnConflict = values.stream().anyMatch(DbColumn::upsertOverwrite);
            if (overwriteOnConflict) {
                sqlStatementBuilder.append(" DO UPDATE SET ");

                for (DbColumn column : values) {
                    if (!column.upsertOverwrite()) {
                        continue;
                    }

                    sqlStatementBuilder.append(column.name()).append(" = EXCLUDED.");
                    sqlStatementBuilder.append(column.name());
                    sqlStatementBuilder.append(", ");
                }

                removeLastTwoCharacters(sqlStatementBuilder); // remove the last unused ", "
            } else {
                sqlStatementBuilder.append(" DO NOTHING");
            }
        }

        String sqlStatement = sqlStatementBuilder.toString();

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = conn.prepareStatement(sqlStatement)) {

            for (int i = 0; i < values.size(); i++) {
                DbColumn column = values.get(i);
                Object value = column.value();
                int type = column.type();

                if (value != null) {
                    statement.setObject(i + 1, value, type);
                } else {
                    statement.setNull(i + 1, type);
                }
            }

            statement.executeUpdate();
        }
    }

    @Override
    public <T> T fetchFirstData(
            Function<Connection, PreparedStatement> sqlGenerator, Function<ResultSet, T> converter)
            throws SQLException {

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = sqlGenerator.apply(conn);
                ResultSet resultSet = statement.executeQuery()) {

            return dataStreamFromResultSet(resultSet, converter).findFirst().orElse(null);
        } catch (Exception e) {
            throw new SQLException(
                    "Some exception occurred while fetching the first data element from the database",
                    e);
        }
    }

    @Override
    public <T, S> S fetchManyData(
            Function<Connection, PreparedStatement> sqlGenerator,
            Function<ResultSet, T> converter,
            Collector<? super T, ?, S> collector)
            throws SQLException {

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = sqlGenerator.apply(conn);
                ResultSet resultSet = statement.executeQuery()) {

            return dataStreamFromResultSet(resultSet, converter).collect(collector);
        } catch (Exception e) {
            throw new SQLException(
                    "Some exception occurred while fetching many data elements from the database",
                    e);
        }
    }

    private <T> Stream<T> dataStreamFromResultSet(
            final ResultSet topLevelResultSet, final Function<ResultSet, T> converter) {

        var resultSetIterator = new ResultSetIterator(topLevelResultSet);
        var stream =
                StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                resultSetIterator, Spliterator.ORDERED | Spliterator.IMMUTABLE),
                        false);

        return stream.map(converter);
    }

    @Override
    public Set<PartnerMetadata> fetchMetadataForMessageLinking(String submissionId)
            throws SQLException, FormatterProcessingException {
        var sql =
                """
                SELECT m2.*
                FROM metadata m1
                JOIN metadata m2
                    ON m1.placer_order_number = m2.placer_order_number
                        AND m1.sending_application_id = m2.sending_application_id
                        AND m1.sending_facility_id = m2.sending_facility_id
                WHERE m1.sent_message_id = ?;
                """;

        try (Connection conn = connectionPool.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, submissionId);
            ResultSet resultSet = statement.executeQuery();

            Set<PartnerMetadata> metadataSet = new HashSet<>();

            while (resultSet.next()) {
                metadataSet.add(partnerMetadataFromResultSet(resultSet));
            }

            return metadataSet;
        }
    }

    private void removeLastTwoCharacters(StringBuilder stringBuilder) {
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
    }
}
