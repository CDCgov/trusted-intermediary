package gov.hhs.cdc.trustedintermediary.external.hapi


import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhirEngine
import spock.lang.Specification

// @todo build actual tests this is a skeleton
class HapiFhirEngineImplementationTest extends Specification {
    HapiFhirEngine engine

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        engine = new HapiFhirEngineImplementation() as HapiFhirEngine
    }

    def cleanup() {
    }

    def "superbasic"() {
        given:
        def expectedResult = null

        when:
        def actualResult = null

        then:
        actualResult === expectedResult
    }

    def "parsePath returns null on blank"() {
        given:
        def expectedResult = null

        when:
        def actualResult = engine.parsePath("")

        then:
        actualResult == expectedResult
    }

    def "parsePath returns not null on a valid"() {
        given:
        def expectedResult = null

        when:
        def result = engine.parsePath("Bundle.entry.resource.ofType(MessageHeader)")

        then:
        result != null
    }
}
