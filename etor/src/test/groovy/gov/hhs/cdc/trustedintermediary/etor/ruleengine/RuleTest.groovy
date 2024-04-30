package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.FhirResourceMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class RuleTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, Mock(Logger))
        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "runRule throws an UnsupportedOperationException when ran from the Rule class"() {
        given:
        def rule = new Rule()

        when:
        rule.runRule(new FhirResourceMock("resource"))

        then:
        thrown(UnsupportedOperationException)
    }
}
