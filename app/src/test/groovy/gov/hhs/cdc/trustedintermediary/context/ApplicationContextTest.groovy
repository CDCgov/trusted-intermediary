package gov.hhs.cdc.trustedintermediary.context

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import spock.lang.Specification

class ApplicationContextTest extends Specification {
    def "singleton object test"() {
        setup:
        def contextA = ApplicationContext.getInstance()
        def contextB = ApplicationContext.getInstance()

        when:
        def result = contextA == contextB

        then:
        result == true
    }

    def "implementation retrieval test"() {
        setup:
        def context = ApplicationContext.getInstance()
        def result = "DogCow"
        context.register(String.class, "DogCow")

        expect:
        result == context.getImplementation(String.class)
    }
}
