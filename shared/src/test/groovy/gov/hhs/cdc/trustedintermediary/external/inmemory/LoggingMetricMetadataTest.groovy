package gov.hhs.cdc.trustedintermediary.external.inmemory

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

class LoggingMetricMetadataTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, LoggingMetricMetadata.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "meta data is correctly logged out"() {
        given:
        var logger = Mock(Logger)
        TestApplicationContext.register(Logger, logger)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LoggingMetricMetadata.getInstance().put("Key", _)

        then:
        1 * logger.logMap(_ as String, _ as Map) >> { String message, Map keyValue ->
            assert keyValue.containsKey("BundleId")
            assert keyValue.containsKey("Entry Time")
            assert keyValue.containsKey("Entry Step")
        }
    }
}
