package gov.hhs.cdc.trustedintermediary.external.inmemory;

import gov.hhs.cdc.trustedintermediary.metadata.MetaDataEntry;
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;
import org.hl7.fhir.r4.model.Bundle;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* TODO: UPDATE ME
 */
public class PrivateMetricMetaData implements MetricMetaData {
    private static final PrivateMetricMetaData INSTANCE = new PrivateMetricMetaData();

    public static PrivateMetricMetaData getInstance() { return INSTANCE; }

    @Inject
    Logger logger;

    private PrivateMetricMetaData(){}

    private static final ConcurrentHashMap<String, MetaDataEntry> metadataMap = new ConcurrentHashMap<>();

    public void put(String bundleId, MetaDataStep step){
        MetaDataEntry entry = extractMetricsFromBundle(bundleId, step);
        metadataMap.put(bundleId, entry);
        logger.logInfo(metadataMap.toString());
    }

    public Map<String, MetaDataEntry> getMetaDataMap(){return metadataMap;}

    private MetaDataEntry extractMetricsFromBundle(String bundleId, MetaDataStep step){
        return new MetaDataEntry(bundleId, step);

    }

    //TODO: Write extractMetricsFromBundle method
    //TODO: Make calls to map wherever in the code a step occurs
    //TODO: Write test for MetaDataEntry Class

}
