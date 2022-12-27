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
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.Slf4jLogger
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DomainRegistrationTest extends Specification {

    def setup() {
        println('Setting up test data...')
        TestApplicationContext.reset()
        ApplicationContext.register(Logger.class, Slf4jLogger.getLogger())
    }

    def "domain registration has endpoints"() {
        given:
        ApplicationContext.register(Formatter, new JacksonFormatter())
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
        def domainRegistration = new DomainRegistration()
        def domainRequest = new DomainRequest()
        def headers = Map.of("Content-Type", "application/json")
        domainRequest.setHeaders(headers)
        def newBody = """{"destination":"fake lab","client":"fake hospital","content":"MSH|lab order"}"""
        domainRequest.setBody(newBody)
        def orderController = OrderController.getInstance()

        def mockParsedBody =
                """\"{\\"id\\":\\"missing id\\",\\"destination\\":\\"fake lab\\",\\"createdAt\\":\\"missing timestamp\\",\\"client\\":\\"fake hospital\\",\\"content\\":\\"MSH|lab order\\"}\""""

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
