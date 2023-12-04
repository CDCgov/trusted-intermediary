package gov.hhs.cdc.trustedintermediary.metadata

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class MetadataEntryTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(MetadataEntry.class)

        then:
        noExceptionThrown()
    }
}
