package gov.hhs.cdc.trustedintermediary.organizations

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class OrganizationTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(Organization.class)

        then:
        noExceptionThrown()
    }
}
