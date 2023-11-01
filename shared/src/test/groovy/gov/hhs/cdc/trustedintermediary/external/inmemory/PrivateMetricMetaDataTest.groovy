package gov.hhs.cdc.trustedintermediary.external.inmemory

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData
import spock.lang.Specification

class PrivateMetricMetaDataTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetaData, PrivateMetricMetaData.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "meta data map is populated"() {

        when:
        PrivateMetricMetaData.getInstance().put("Key", MetaDataStep.RECEIVED_FROM_REPORT_STREAM)

        then:
        PrivateMetricMetaData.getInstance().getMetaDataMap().containsKey("Key")
    }
}
