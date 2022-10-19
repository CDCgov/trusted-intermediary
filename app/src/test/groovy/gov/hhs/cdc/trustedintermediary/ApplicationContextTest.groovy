package gov.hhs.cdc.trustedintermediary.context

import spock.lang.Specification

class ApplicationContextTest extends Specification {
    def "singleton object"() {
        setup:
        def contextA = ApplicationContext.getInstance()
        def contextB = ApplicationContext.getInstance()

        when:
        def result = contextA == contextB?true:false

        then:
        result == true
    }
}
