package gov.hhs.cdc.trustedintermediary.context

import spock.lang.Specification

class ApplicationContextTest extends Specification {

    def "implementation retrieval test"() {
        setup:
        def result = "DogCow"
        ApplicationContext.register(String.class, "DogCow")

        expect:
        result == ApplicationContext.getImplementation(String.class)
    }
}
