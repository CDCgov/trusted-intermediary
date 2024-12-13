package gov.hhs.cdc.trustedintermediary.rse2e.hl7


import spock.lang.Specification

class HL7FileMatcherExceptionTest extends Specification {

    def "two param constructor works"() {
        given:
        def message = "something blew up!"
        def cause = new NullPointerException()

        when:
        def exception = new HL7FileMatcherException(message, cause)

        then:
        exception.getMessage() == message
        exception.getCause() == cause
    }

    def "single param constructor works"() {
        given:
        def message = "something blew up!"

        when:
        def exception = new HL7FileMatcherException(message)

        then:
        exception.getMessage() == message
    }
}
