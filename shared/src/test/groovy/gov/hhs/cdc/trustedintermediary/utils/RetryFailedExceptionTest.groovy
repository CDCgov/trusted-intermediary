package gov.hhs.cdc.trustedintermediary.utils

import spock.lang.Specification

class RetryFailedExceptionTest extends Specification {
    def "test constructor"() {
        given:
        def message = "DogCow"

        when:
        def innerException = new IOException()
        def exception = new RetryFailedException(message, innerException)

        then:
        exception.getMessage() == message
        exception.getCause() == innerException
    }
}
