package gov.hhs.cdc.trustedintermediary.external.database;

/** This interface represents a provider for retrieving database credentials. */
public interface DatabaseCredentialsProvider {
    String getPassword();
}
