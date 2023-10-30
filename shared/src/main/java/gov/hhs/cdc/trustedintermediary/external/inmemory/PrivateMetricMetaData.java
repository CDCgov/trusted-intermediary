package gov.hhs.cdc.trustedintermediary.external.inmemory;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateMetricMetaData implements MetricMetaData {
    private static final PrivateMetricMetaData INSTANCE = new PrivateMetricMetaData();

    public static PrivateMetricMetaData getInstance() { return INSTANCE; }

    @Inject
    Logger logger;

    private PrivateMetricMetaData(){}

    private static final ConcurrentHashMap<String, String> metadataMap = new ConcurrentHashMap<>();

    public void put(String key, String value){
        metadataMap.put(key, value);
        logger.logInfo(metadataMap.toString());
    }

    public Map<String, String> getMetaDataMap(){return metadataMap;}
}
