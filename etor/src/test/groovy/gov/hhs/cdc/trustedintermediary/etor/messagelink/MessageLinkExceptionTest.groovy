package gov.hhs.cdc.trustedintermediary.etor.messagelink

import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import spock.lang.Specification

class MessageLinkExceptionTest extends Specification {
    def "constructor works"() {
        given:
        def message = "exception message"
        def cause = new FormatterProcessingException(message, new IOException())

        when:
        def exceptionWithoutCause = new MessageLinkException(message)
        def exceptionWithCause = new MessageLinkException(message, cause)

        then:
        exceptionWithCause.getMessage() == message
        exceptionWithCause.getCause() == cause
        exceptionWithoutCause.getMessage() == message
    }
}
