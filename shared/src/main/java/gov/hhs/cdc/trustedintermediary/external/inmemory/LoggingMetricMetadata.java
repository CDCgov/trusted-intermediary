package gov.hhs.cdc.trustedintermediary.external.inmemory;

import gov.hhs.cdc.trustedintermediary.metadata.MetadataEntry;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import java.util.Map;
import javax.inject.Inject;

/**
 * Implementation of a class that can be used throughout the project to collect a list of metadata
 * events
 */
public class LoggingMetricMetadata implements MetricMetadata {
    private static final LoggingMetricMetadata INSTANCE = new LoggingMetricMetadata();

    @Inject Logger logger;

    public static LoggingMetricMetadata getInstance() {
        return INSTANCE;
    }

    private LoggingMetricMetadata() {}

    @Override
    public <T> void put(String bundleId, T step) {
        MetadataEntry<T> entry = extractMetricsFromBundle(bundleId, step);
        var metadataMap =
                Map.of(
                        "BundleId",
                        entry.bundleId(),
                        "Entry Time",
                        entry.entryTime(),
                        "Entry Step",
                        entry.entryStep());

        logger.logMap("Metadata Event Occurred:", metadataMap);
    }

    private <T> MetadataEntry<T> extractMetricsFromBundle(String bundleId, T step) {
        return new MetadataEntry<>(bundleId, step);
    }
}
