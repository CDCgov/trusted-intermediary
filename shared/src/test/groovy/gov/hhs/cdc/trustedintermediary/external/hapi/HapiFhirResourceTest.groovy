package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class HapiFhirResourceTest extends Specification {

    def mockLogger = Mock(Logger)
    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
    }

    def "sample test run to see if logger is injected"() {
        given:
        def expectedBundle = new Bundle()
        when:
        def resource = new HapiFhirResource(expectedBundle)
        def actualBundle = resource.getUnderlyingData()
        then:
        expectedBundle == actualBundle
    }
}
