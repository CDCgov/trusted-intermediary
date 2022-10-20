package gov.hhs.cdc.trustedintermediary.domainconnector

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class DomainRequestTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(DomainRequest.class)

        then:
        noExceptionThrown()
    }
}
