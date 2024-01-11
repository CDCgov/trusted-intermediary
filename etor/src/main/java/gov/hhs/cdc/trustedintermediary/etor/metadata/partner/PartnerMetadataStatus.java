package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

/**
 * Enum for tracking the status of a message for diagnostic purposes. We store the status in our
 * database
 */
public enum PartnerMetadataStatus {
    PENDING,
    DELIVERED,
    FAILED
}
