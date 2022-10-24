package gov.hhs.cdc.trustedintermediary.domainconnector

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class HttpEndpointTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(HttpEndpoint.class)

        then:
        noExceptionThrown()
    }

    def "test constructor"() {
        given:
        def verb = "POST"
        def path = "/dogcow"

        when:
        def httpEndpoint = new HttpEndpoint(verb, path)

        then:
        httpEndpoint.getVerb() == verb
        httpEndpoint.getPath() == path
    }
}
