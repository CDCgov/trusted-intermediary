package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.DbDao;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using a database. */
public class DatabasePartnerMetadataStorage implements PartnerMetadataStorage {

    @Inject DbDao dao;

    @Inject Logger logger;
    private static final DatabasePartnerMetadataStorage INSTANCE =
            new DatabasePartnerMetadataStorage();

    private DatabasePartnerMetadataStorage() {}

    public static DatabasePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {

        try {
            ResultSet result = dao.fetchMetadata("receiverName");
            while (result.next()) {
                logger.logInfo(result.getString(5));
                return new PartnerMetadata(
                        result.getString(1),
                        result.getString(2),
                        result.getString(3),
                        Timestamp.valueOf(result.getString(4)).toInstant(),
                        result.getString(5));
            }

        } catch (Exception e) {
            logger.logError("Error reading data: " + e.getMessage());
        }
        return null;
    }
    // TODO: Should this be separate or should we have the DAO stuff happen in here
    @Override
    public void saveMetadata(final PartnerMetadata metadata) {
        try {
            dao.upsertMetadata(
                    metadata.uniqueId(),
                    metadata.sender(),
                    metadata.receiver(),
                    metadata.hash(),
                    metadata.timeReceived());
            // TODO: Put response into the logger
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
