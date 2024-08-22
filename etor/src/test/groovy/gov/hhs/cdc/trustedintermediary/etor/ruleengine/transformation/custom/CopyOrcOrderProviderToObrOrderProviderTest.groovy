package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata


class CopyOrcOrderProviderToObrOrderProviderTest {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new AddEtorProcessingTag()
    }

    def "add ETOR meta tag to message header"() {
    }
}

import spock.lang.Specification
