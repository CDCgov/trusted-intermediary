package gov.hhs.cdc.trustedintermediary.metadata

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class MetaDataEntryTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(MetaDataEntry.class)

        then:
        noExceptionThrown()
    }
}
