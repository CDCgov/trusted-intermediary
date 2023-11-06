package gov.hhs.cdc.trustedintermediary.external.inmemory;

import gov.hhs.cdc.trustedintermediary.metadata.MetaDataEntry;
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;
import java.util.Map;
import javax.inject.Inject;

/**
 * Implementation of a class that can be used throughout the project to collect a list of metadata
 * events
 */
public class LoggingMetricMetaData implements MetricMetaData {
    private static final LoggingMetricMetaData INSTANCE = new LoggingMetricMetaData();

    @Inject Logger logger;

    public static LoggingMetricMetaData getInstance() {
        return INSTANCE;
    }

    private LoggingMetricMetaData() {}

    @Override
    public void put(String bundleId, MetaDataStep step) {
        MetaDataEntry entry = extractMetricsFromBundle(bundleId, step);
        var metadataMap =
                Map.of(
                        "BundleId",
                        entry.bundleId(),
                        "Entry Time",
                        entry.entryTime(),
                        "Entry Step",
                        entry.entryStep());

        logger.logMap("MetaData Event Occurred:", metadataMap);
    }

    private MetaDataEntry extractMetricsFromBundle(String bundleId, MetaDataStep step) {
        return new MetaDataEntry(bundleId, step);
    }
}
