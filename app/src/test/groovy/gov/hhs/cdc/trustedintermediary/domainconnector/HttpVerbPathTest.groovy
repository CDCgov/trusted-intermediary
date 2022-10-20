package gov.hhs.cdc.trustedintermediary.domainconnector

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class HttpVerbPathTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(HttpVerbPath.class)

        then:
        noExceptionThrown()
    }

    def "test constructor"() {
        given:
        def verb = "POST"
        def path = "/dogcow"

        when:
        def httpVerbPath = new HttpVerbPath(verb, path)

        then:
        httpVerbPath.getVerb() == verb
        httpVerbPath.getPath() == path
    }
}
