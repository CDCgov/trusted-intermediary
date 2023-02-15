package gov.hhs.cdc.trustedintermediary.external.hapi

import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class HapiLabOrderTest extends Specification {
    def "getUnderlyingOrder Works"() {
        given:
        def expectedInnerLabOrder = new Bundle()
        def labOrder = new HapiLabOrder(expectedInnerLabOrder)

        when:
        def actualInnerLabOrder = labOrder.getUnderlyingOrder()

        then:
        actualInnerLabOrder == expectedInnerLabOrder
    }
}
