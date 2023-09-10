package gov.hhs.cdc.trustedintermediary.domainconnector

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class DomainResponseTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(DomainResponse.class)

        then:
        noExceptionThrown()
    }

    def "test constructor"() {
        given:
        def status = 418

        when:
        def response = new DomainResponse(status)

        then:
        response.getStatusCode() == status
    }
}
