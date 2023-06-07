package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class AuthRequestTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(AuthRequest)

        then:
        noExceptionThrown()
    }

    def "test equals and hashCode"() {
        when:
        PojoTestUtils.validateEqualsAndHashCode(AuthRequest)

        then:
        noExceptionThrown()
    }
}
