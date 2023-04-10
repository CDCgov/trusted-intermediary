package gov.hhs.cdc.trustedintermediary.wrappers


import spock.lang.Specification

class TokenGenerationExceptionTest extends Specification {
    def "constructor works"() {
        given:
        def errorMessage = "DogCow goes Moof"
        def cause = new NullPointerException()

        when:
        def exception = new TokenGenerationException(errorMessage, cause)

        then:
        exception.getMessage() == errorMessage
        exception.getCause() == cause
    }
}
