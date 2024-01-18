package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

/** Custom exception class use to catch partner metadata exceptions */
public class PartnerMetadataException extends Exception {

    public PartnerMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public PartnerMetadataException(String message) {
        super(message);
    }
}
