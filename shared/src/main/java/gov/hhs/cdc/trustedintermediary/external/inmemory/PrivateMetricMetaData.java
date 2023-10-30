package gov.hhs.cdc.trustedintermediary.external.inmemory;

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

    private static final ConcurrentHashMap<String, String> metadataMap = new ConcurrentHashMap<>();

    public void put(String key, Bundle bundle){
        String sanitizedBundle = extractMetricsFromBundle(bundle);
        metadataMap.put(key, sanitizedBundle);
        logger.logInfo(metadataMap.toString());
    }

    public Map<String, String> getMetaDataMap(){return metadataMap;}

    private String extractMetricsFromBundle(Bundle bundle){


    }

    //TODO: Write extractMetricsFromBundle method
    //TODO: Create Enum Class for different steps
    //TODO: Add timestamps for step
    //TODO: Make calls to map wherever in the code a step occurs

}
