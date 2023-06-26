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
        def resourceId = "67890asdfg"
        def patientId = "fthgyu687"

        def orders = new OrdersMock(resourceId, patientId, null)

        when:
        def response = new OrdersResponse(orders)

        then:
        response.getFhirResourceId() == resourceId
        response.getPatientId() == patientId
    }

    def "test argument constructor"() {
        given:
        def resourceId = "67890asdfg"
        def patientId = "fthgyu687"

        when:
        def response = new OrdersResponse(resourceId, patientId)

        then:
        response.getFhirResourceId() == resourceId
        response.getPatientId() == patientId
    }
}
