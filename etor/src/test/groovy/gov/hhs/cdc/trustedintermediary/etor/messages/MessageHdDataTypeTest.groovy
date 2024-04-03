package gov.hhs.cdc.trustedintermediary.etor.messages

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class MessageHdDataTypeTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(MessageHdDataType)

        then:
        noExceptionThrown()
    }
}
