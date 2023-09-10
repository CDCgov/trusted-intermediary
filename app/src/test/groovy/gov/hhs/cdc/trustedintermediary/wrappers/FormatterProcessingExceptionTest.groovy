package gov.hhs.cdc.trustedintermediary.wrappers

import gov.hhs.cdc.trustedintermediary.external.formatter.FormatterProcessingException
import spock.lang.Specification

class FormatterProcessingExceptionTest extends Specification {
    def "test constructor"() {
        when:
        def message = "DogCow"
        def innerException = new NullPointerException()
        def exception = new FormatterProcessingException(message, innerException)

        then:
        exception.getMessage() == message
        exception.getCause() == innerException
    }
}
