package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrdersMock
import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class OrdersResponseTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(OrdersResponse.class)

        then:
        noExceptionThrown()
    }

    def "test demographics constructor"() {
        given:
        def expectedResourceId = "67890asdfg"
        def expectedPatientId = "fthgyu687"

        def orders = new OrdersMock(expectedResourceId, expectedPatientId, null)

        when:
        def actual = new OrdersResponse(orders)

        then:
        actual.getFhirResourceId() == expectedResourceId
        actual.getPatientId() == expectedPatientId
    }

    def "test argument constructor"() {
        given:
        def expectedResourceId = "67890asdfg"
        def expectedPatientId = "fthgyu687"

        when:
        def actual = new OrdersResponse(expectedResourceId, expectedPatientId)

        then:
        actual.getFhirResourceId() == expectedResourceId
        actual.getPatientId() == expectedPatientId
    }
}
