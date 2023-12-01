package gov.hhs.cdc.trustedintermediary.etor.metadata;

public interface PartnerMetaDataStorage {
    PartnerMetadata readMetadata(String uniqueId);

    void saveMetadata(PartnerMetadata metadata);

    // Eh?  What do you all think about that?  Do we think this is a good interface?  Fairly simple.
    //  Too simple?
    // saveMetadata could do upserts for either the file or database implementation?
}
