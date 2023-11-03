package gov.hhs.cdc.trustedintermediary.metadata;

import java.util.Date;

/** An instance of a metadata event to be used for internal troubleshooting of messages */
public class MetaDataEntry {

    public MetaDataStep entryStep;
    public Date entryTime;
    public String bundleId;

    public MetaDataEntry(String bundleId, MetaDataStep step) {
        this.entryTime = new Date();
        this.bundleId = bundleId;
        this.entryStep = step;
    }
}
