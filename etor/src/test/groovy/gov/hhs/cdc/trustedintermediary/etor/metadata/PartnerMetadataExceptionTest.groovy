package gov.hhs.cdc.trustedintermediary.etor.metadata

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import spock.lang.Specification

class PartnerMetadataExceptionTest extends Specification {
    def "two param constructor works"() {
        given:
        def message = "something blew up!"
        def cause = new NullPointerException()

        when:
        def exception = new PartnerMetadataException(message, cause)

        then:
        exception.getMessage() == message
        exception.getCause() == cause
    }

    def "single param constructor works"() {
        given:
        def message = "something blew up!"

        when:
        def exception = new PartnerMetadataException(message)

        then:
        exception.getMessage() == message
    }
}
