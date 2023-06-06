package gov.hhs.cdc.trustedintermediary.organizations


import spock.lang.Specification

class OrganizationsSettingsExceptionTest extends Specification {
    def "test constructor"() {
        when:
        def message = "DogCow"
        def innerException = new NullPointerException()
        def exception = new OrganizationsSettingsException(message, innerException)

        then:
        exception.getMessage() == message
        exception.getCause() == innerException
    }
}
