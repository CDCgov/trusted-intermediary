package gov.hhs.cdc.trustedintermediary.metadata;

import java.time.Instant;

/** An instance of a metadata event to be used for internal troubleshooting of messages */
public record MetaDataEntry<T>(String bundleId, T entryStep, Instant entryTime) {

    public MetaDataEntry(String bundleId, T entryStep) {
        this(bundleId, entryStep, Instant.now());
    }
}
