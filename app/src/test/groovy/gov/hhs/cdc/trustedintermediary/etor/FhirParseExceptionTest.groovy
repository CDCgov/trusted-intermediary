package gov.hhs.cdc.trustedintermediary.etor

import spock.lang.Specification

class FhirParseExceptionTest extends Specification {

    def "constructor works"() {
        given:
        def message = "DogCow blew up!"
        def cause = new Exception(message, new IllegalArgumentException())

        when:
        def exception = new FhirParseException(message, cause)

        then:
        exception.getMessage() == message
        exception.getCause() == cause
    }
}
