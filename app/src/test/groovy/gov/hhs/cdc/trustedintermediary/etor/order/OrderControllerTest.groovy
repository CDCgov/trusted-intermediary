package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.external.javalin.App
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.JacksonFormatter
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.Slf4jLogger
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderControllerTest extends Specification {

    def setup() {
        println('Setting up test data...')
        TestApplicationContext.reset()
        ApplicationContext.register(Logger.class, Slf4jLogger.getLogger())
    }

    def "parseOrder works"() {
        given:
        ApplicationContext.register(Formatter.class, new JacksonFormatter())
        ApplicationContext.register(DomainRequest.class, new DomainRequest())
        def destination = "fake lab"
        def client = "fake client"
        def order = new Order()
        order.setClient(client)
        order.setDestination(destination)
        order.setBody("")
        def orderMessage = new OrderMessage()
        def request = ApplicationContext.getImplementation(DomainRequest)
        def headers = Map.of("Client", client, "Destination", destination)
        request.setHeaders(headers)
        when:
        def parsedOrder = OrderController.getInstance().parseOrder(request)

        then:
        parsedOrder.getId() == order.getId()
        parsedOrder.getClient() == order.getClient()
        parsedOrder.getDestination() == order.getDestination()
        parsedOrder.getBody() == order.getBody()
    }

    def "constructOrderMessage works"() {

        given:
        TestApplicationContext.register(Formatter, new JacksonFormatter())
        // TestApplicationContext.register(OrderMessage, new OrderMessage())
        def orderId = "1234abcd"
        def destination = "fake lab"
        def client = "fake client"
        def body = "MSH|lab order"
        LocalDateTime createAt = LocalDateTime.now(ZoneId.of("UTC"))
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
        def formattedDateTime = createAt.format(dateTimeFormat)
        Order order = new Order(orderId, destination, createAt, client, body)
        def orderController = OrderController.getInstance()

        def expected =
                """{
  "id" : "1234abcd",
  "destination" : "fake lab",
  "createdAt" : "$formattedDateTime",
  "client" : "fake client",
  "body" : "MSH|lab order"
}"""

        when:
        def orderMessage = orderController.constructOrderMessage(order)

        then:
        expected == orderMessage
    }
}
