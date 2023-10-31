package gov.hhs.cdc.trustedintermediary.wrappers;

import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;
import org.hl7.fhir.r4.model.Bundle;

public interface MetricMetaData {

    void put(String value, MetaDataStep step);
}
