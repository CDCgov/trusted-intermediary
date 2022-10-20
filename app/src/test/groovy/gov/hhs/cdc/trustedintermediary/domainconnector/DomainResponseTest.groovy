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
}
