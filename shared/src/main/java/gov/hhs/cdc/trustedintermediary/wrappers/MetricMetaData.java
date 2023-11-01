package gov.hhs.cdc.trustedintermediary.wrappers;

import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;

public interface MetricMetaData {

    void put(String value, MetaDataStep step);
}
