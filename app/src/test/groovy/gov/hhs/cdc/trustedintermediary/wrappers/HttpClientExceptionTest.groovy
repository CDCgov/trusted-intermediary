package gov.hhs.cdc.trustedintermediary.wrappers

import spock.lang.Specification

class HttpClientExceptionTest extends Specification {
    def "test constructor"() {
        given:
        def message = "DogCow"

        when:
        def innerException = new IOException()
        def exception = new HttpClientException(message,innerException)

        then:
        exception.getMessage() == message
        exception.getCause() == innerException
    }
}
