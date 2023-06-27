package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import spock.lang.Specification

class OrdersControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrdersController, OrdersController.getInstance())
        TestApplicationContext.register(DomainResponseHelper, DomainResponseHelper.getInstance())
    }

    def "constructResponse works"() {
        given:
        def controller = OrdersController.getInstance()
        def mockHelper = Mock(DomainResponseHelper)
        def statusCode = 200
        def expected = statusCode
        def orderResponse = new OrdersResponse("asdf-12341-jkl-7890", "blkjh-7685")
        mockHelper.constructOkResponse(_ as OrdersResponse) >> new DomainResponse(statusCode)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = controller.constructResponse(orderResponse).statusCode

        then:
        actual == expected
    }
}
