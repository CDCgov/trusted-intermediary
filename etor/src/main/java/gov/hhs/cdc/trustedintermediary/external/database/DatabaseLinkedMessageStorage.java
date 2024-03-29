package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.SQLException;
import java.util.Set;
import javax.inject.Inject;

public class DatabaseLinkedMessageStorage {

    @Inject DbDao dao;

    @Inject Logger logger;

    private static final DatabaseLinkedMessageStorage INSTANCE = new DatabaseLinkedMessageStorage();

    private DatabaseLinkedMessageStorage() {}

    public static DatabaseLinkedMessageStorage getInstance() {
        return INSTANCE;
    }

    public Set<?> readLinkedMessages(String submissionId) throws Exception {
        Set<?> linkedMessageSet;
        try {
            linkedMessageSet = dao.fetchLinkedMessages(submissionId);
        } catch (SQLException e) {
            throw new Exception("Error retrieving metadata", e);
        }
        return linkedMessageSet;
    }
}
