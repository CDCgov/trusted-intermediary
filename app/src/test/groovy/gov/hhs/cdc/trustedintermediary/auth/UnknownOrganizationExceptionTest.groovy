package gov.hhs.cdc.trustedintermediary.auth


import spock.lang.Specification

class UnknownOrganizationExceptionTest extends Specification {
    def "construction works"() {
        given:
        def message = "DogCow goes moof"

        when:
        def exception = new UnknownOrganizationException(message)

        then:
        exception.getMessage() == message
    }
}
