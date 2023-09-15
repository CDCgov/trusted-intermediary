package gov.hhs.cdc.trustedintermediary.wrappers

import spock.lang.Specification

class YamlCombinerExceptionTest extends Specification {
    def "test constructor"() {
        when:
        def message = "DogCow"
        def innerException = new NullPointerException()
        def exception = new YamlCombinerException(message, innerException)

        then:
        exception.getMessage() == message
        exception.getCause() == innerException
    }
}
