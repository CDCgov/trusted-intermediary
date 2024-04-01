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

    def "toString() happy path works"() {
        given:
        def namespace = "mock-namespace"
        def universalId = "mock-universal-id"
        def universalIdType = "mock-universal-id-type"
        def expected = "${namespace}^${universalId}^${universalIdType}"

        when:
        def messageDetails = new MessageHdDataType(namespace, universalId, universalIdType)
        def actual = messageDetails.toString()

        then:
        actual == expected
    }

    def "toString() with null variable unhappy path works"() {
        given:
        def namespace = "mock-namespace"
        def universalId = null
        def universalIdType = "mock-universal-id-type"
        def expected = "${namespace}^${universalIdType}"

        when:
        def messageDetails = new MessageHdDataType(namespace, universalId, universalIdType)
        def actual = messageDetails.toString()

        then:
        actual == expected
    }

    def "toString() with all null variables unhappy path works"() {
        given:
        def namespace = null
        def universalId = null
        def universalIdType = null
        def expected = ""

        when:
        def messageDetails = new MessageHdDataType(namespace, universalId, universalIdType)
        def actual = messageDetails.toString()

        then:
        actual == expected
    }
}
