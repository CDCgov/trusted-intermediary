package gov.hhs.cdc.trustedintermediary.external.inmemory

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData
import spock.lang.Specification

import java.nio.file.Paths

class PrivateMetricMetaDataTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetaData, PrivateMetricMetaData.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "meta data map is populated"() {

        when:
        PrivateMetricMetaData.getInstance().put("Key", "mock Value")

        then:
        PrivateMetricMetaData.getInstance().getMetaDataMap().containsKey("Key")
    }


    def "we never ever log phi or pii"() {
        when:
        PrivateMetricMetaData.getInstance().put("key","{resourceType: 'Patient'}" )

        then:
        !PrivateMetricMetaData.getInstance().getMetaDataMap().get("key").contains("Patient")
    }

}
