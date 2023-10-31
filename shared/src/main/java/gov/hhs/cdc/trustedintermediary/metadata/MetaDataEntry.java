package gov.hhs.cdc.trustedintermediary.metadata;

import java.util.Date;

//TODO: Add class doc
public class MetaDataEntry {

    private MetaDataStep entryStep;
    private Date entryTime;
    private String bundleId;
    public MetaDataEntry(String bundleId, MetaDataStep step){
        this.entryTime = new Date();
        this.bundleId = bundleId;
        this.entryStep = step;

    }


}
