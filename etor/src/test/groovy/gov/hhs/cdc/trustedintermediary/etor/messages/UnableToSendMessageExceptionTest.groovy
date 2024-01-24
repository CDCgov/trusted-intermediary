package gov.hhs.cdc.trustedintermediary.etor.messages

import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException
import spock.lang.Specification

class UnableToSendMessageExceptionTest extends Specification {
    def "contructor works"() {

        given:
        def message = "something blew up!"
        def cause = new HttpClientException(message, new IOException())

        when:
        def exception = new UnableToSendMessageException(message, cause)

        then:
        exception.getMessage() == message
        exception.getCause() == cause
    }
}
