package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class OrdersControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrdersController, OrdersController.getInstance())
    }

    def "parseOrder happy path works"() {
        given:
        def request = new DomainRequest()
        def controller = OrdersController.getInstance()
        def bundle = new Bundle()
        def expected = bundle
        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class) >> bundle
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = controller.parseOrders(request).underlyingOrder

        then:
        actual == expected
    }
}
