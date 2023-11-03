package gov.hhs.cdc.trustedintermediary.external.inmemory

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData
import spock.lang.Specification

class LoggingMetricMetaDataTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetaData, LoggingMetricMetaData.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "meta data map is populated"() {

        when:
        LoggingMetricMetaData.getInstance().put("Key", _ as MetaDataStep)

        then:
        LoggingMetricMetaData.getInstance().getMetaDataMap().containsKey("BundleId")
    }
}
