package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class OrderResponseTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(OrderResponse.class)

        then:
        noExceptionThrown()
    }

    def "test orders constructor"() {
        given:
        def expectedResourceId = "67890asdfg"
        def expectedPatientId = "fthgyu687"

        def orders = new OrderMock(expectedResourceId, expectedPatientId, null)

        when:
        def actual = new OrderResponse(orders)

        then:
        actual.getFhirResourceId() == expectedResourceId
        actual.getPatientId() == expectedPatientId
    }

    def "test argument constructor"() {
        given:
        def expectedResourceId = "67890asdfg"
        def expectedPatientId = "fthgyu687"

        when:
        def actual = new OrderResponse(expectedResourceId, expectedPatientId)

        then:
        actual.getFhirResourceId() == expectedResourceId
        actual.getPatientId() == expectedPatientId
    }
}
