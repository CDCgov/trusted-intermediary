package gov.hhs.cdc.trustedintermediary.metadata;

import java.util.Date;


/**
 *  An instance of a metadata event to be used for internal troubleshooting of messages
 */

public class MetaDataEntry {

    private MetaDataStep entryStep;
    private Date entryTime;
    private String bundleId;

    public MetaDataEntry(String bundleId, MetaDataStep step) {
        this.entryTime = new Date();
        this.bundleId = bundleId;
        this.entryStep = step;
    }
}
