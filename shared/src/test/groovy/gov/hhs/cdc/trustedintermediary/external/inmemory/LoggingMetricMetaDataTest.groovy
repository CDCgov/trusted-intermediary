package gov.hhs.cdc.trustedintermediary.external.inmemory

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData
import spock.lang.Specification

class LoggingMetricMetaDataTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetaData, LoggingMetricMetaData.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "meta data is correctly logged out"() {
        given:
        var logger = Mock(Logger)
        TestApplicationContext.register(Logger, logger)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LoggingMetricMetaData.getInstance().put("Key", _ as MetaDataStep)

        then:
        1 * logger.logMap(_ as String, _ as Map) >> { String message, Map keyValue ->
            assert keyValue.containsKey("BundleId")
            assert keyValue.containsKey("Entry Time")
            assert keyValue.containsKey("Entry Step")
        }
    }
}
