package gov.hhs.cdc.trustedintermediary.external.inmemory;

import gov.hhs.cdc.trustedintermediary.metadata.MetaDataEntry;
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

/**
 * Implementation of a class that can be used throughout the project to collect a list of metadata
 * events
 */
public class LoggingMetricMetaData implements MetricMetaData {
    private static final LoggingMetricMetaData INSTANCE = new LoggingMetricMetaData();

    public static LoggingMetricMetaData getInstance() {
        return INSTANCE;
    }

    @Inject Logger logger;

    private LoggingMetricMetaData() {}

    private static final Map<String, Object> metadataMap = new ConcurrentHashMap<>();

    @Override
    public void put(String bundleId, MetaDataStep step) {
        MetaDataEntry entry = extractMetricsFromBundle(bundleId, step);
        metadataMap.put("BundleId", entry.bundleId());
        metadataMap.put("Entry Time", entry.entryTime());
        metadataMap.put("Entry Step", entry.entryStep());
        logger.logMap("MetaData Event Occured:", metadataMap);
    }

    public Map<String, Object> getMetaDataMap() {
        return metadataMap;
    }

    private MetaDataEntry extractMetricsFromBundle(String bundleId, MetaDataStep step) {
        return new MetaDataEntry(bundleId, step);
    }

    // Make calls to map wherever in the code a step occurs
}
