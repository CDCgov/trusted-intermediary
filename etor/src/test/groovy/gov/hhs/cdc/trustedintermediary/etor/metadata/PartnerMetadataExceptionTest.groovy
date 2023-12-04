package gov.hhs.cdc.trustedintermediary.etor.metadata


import spock.lang.Specification

class PartnerMetadataExceptionTest extends Specification {
    def "constructor works"() {

        given:
        def message = "something blew up!"
        def cause = new NullPointerException()

        when:
        def exception = new PartnerMetadataException(message, cause)

        then:
        exception.getMessage() == message
        exception.getCause() == cause
    }
}
