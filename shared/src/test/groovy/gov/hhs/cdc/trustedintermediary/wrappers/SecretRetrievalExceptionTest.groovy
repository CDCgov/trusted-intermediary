package gov.hhs.cdc.trustedintermediary.wrappers

import spock.lang.Specification

class SecretRetrievalExceptionTest extends Specification {
    def "construction works"() {
        given:
        def message = "DogCow goes moof"
        def cause = new NullPointerException()

        when:
        def exception = new SecretRetrievalException(message, cause)

        then:
        exception.getMessage() == message
        exception.getCause() == cause
    }
}
