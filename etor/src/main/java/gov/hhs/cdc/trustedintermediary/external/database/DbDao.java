package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/** Interface for accessing the database for metadata */
public interface DbDao {
    void upsertData(String tableName, List<DbColumn> values, String conflictColumnName)
            throws SQLException;

    <T> T fetchFirstData(
            Function<Connection, PreparedStatement> sqlGenerator, Function<ResultSet, T> converter)
            throws SQLException;

    <T, S> S fetchManyData(
            Function<Connection, PreparedStatement> sqlGenerator,
            Function<ResultSet, T> converter,
            Collector<? super T, ?, S> collector)
            throws SQLException;

    Set<PartnerMetadata> fetchMetadataForSender(String sender)
            throws SQLException, FormatterProcessingException;

    Object fetchMetadata(String uniqueId) throws SQLException, FormatterProcessingException;
}
