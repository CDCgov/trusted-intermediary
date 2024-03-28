package gov.hhs.cdc.trustedintermediary.external.database;

public record DbColumn(String name, Object value, boolean upsertOverwrite, int type) {}
