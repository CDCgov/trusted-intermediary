package gov.hhs.cdc.trustedintermediary.etor

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.etor.order.Order
import gov.hhs.cdc.trustedintermediary.etor.order.OrderController
import gov.hhs.cdc.trustedintermediary.etor.order.OrderMessage
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.JacksonFormatter
import spock.lang.Specification

class DomainRegistrationTest extends Specification {

    def setup() {
        println('Setting up test data...')
        TestApplicationContext.reset()
    }

    def "domain registration has endpoints"() {
        given:
        def domainRegistration = new DomainRegistration()
        def specifiedEndpoint = new HttpEndpoint("POST", "/v1/etor/order")

        when:
        def endpoints = domainRegistration.domainRegistration()

        then:
        !endpoints.isEmpty()
        endpoints.get(specifiedEndpoint) != null
    }

    def "handles an order"() {
        given:
        ApplicationContext.register(Formatter, new JacksonFormatter())
        ApplicationContext.register(OrderMessage, new OrderMessage())
        ApplicationContext.register(Order, new Order("56789fghij",null,null,"DogCow"))
        def domainRegistration = new DomainRegistration()
        def domainRequest = new DomainRequest()
        def headers = Map.of("Destination", "fake lab", "Client", "fake hospital")
        domainRequest.setHeaders(headers)
        domainRequest.setBody("{\"content\":\"MSH|fake lab\"}")
        def orderController = OrderController.getInstance()
        def mockParsedBody = "DogCow"
        orderController.parseOrder(mockParsedBody)
        TestApplicationContext.register(OrderController,orderController)
        TestApplicationContext.register(DomainRegistration,domainRegistration)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = domainRegistration.handleOrder(domainRequest)

        then:
        response.getStatusCode() < 300
        response.getBody() == mockParsedBody
    }
}
