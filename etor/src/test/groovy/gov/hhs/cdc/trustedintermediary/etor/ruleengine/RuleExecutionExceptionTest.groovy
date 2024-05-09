package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import spock.lang.Specification

class RuleExecutionExceptionTest extends Specification {
    def "constructor works"() {
        given:
        def message = "something blew up!"
        def cause = new NullPointerException()

        when:
        def exception = new RuleExecutionException(message, cause)

        then:
        exception.getMessage() == message
        exception.getCause() == cause
    }
}
