package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.DbDao;
import java.sql.SQLException;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using a database. */
public class DatabasePartnerMetadataStorage implements PartnerMetadataStorage {

    @Inject DbDao dao;
    private static final DatabasePartnerMetadataStorage INSTANCE =
            new DatabasePartnerMetadataStorage();

    private DatabasePartnerMetadataStorage() {}

    public static DatabasePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
