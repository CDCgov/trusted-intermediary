package gov.hhs.cdc.trustedintermediary.domainconnector

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class HttpEndpointTest extends Specification {

    def "test constructor"() {
        given:
        def verb = "POST"
        def path = "/dogcow"

        when:
        def httpEndpoint = new HttpEndpoint(verb, path)

        then:
        httpEndpoint.verb() == verb
        httpEndpoint.path() == path
    }

    def "test equals and hashCode"() {
        when:
        PojoTestUtils.validateEqualsAndHashCode(HttpEndpoint.class)

        then:
        noExceptionThrown()
    }
}
