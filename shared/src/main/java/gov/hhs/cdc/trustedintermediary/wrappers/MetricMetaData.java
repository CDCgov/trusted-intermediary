package gov.hhs.cdc.trustedintermediary.wrappers;

import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;

/** Interface to provide a blueprint for working with metadate */
public interface MetricMetaData {

    void put(String value, MetaDataStep step);
}
