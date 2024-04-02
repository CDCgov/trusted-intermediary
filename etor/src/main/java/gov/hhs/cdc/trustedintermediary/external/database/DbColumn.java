package gov.hhs.cdc.trustedintermediary.external.database;

/** Represents a database column for upserting data. */
public record DbColumn(String name, Object value, boolean upsertOverwrite, int type) {}
