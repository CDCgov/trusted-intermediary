package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.javalin.App
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.JacksonFormatter
import spock.lang.Specification

import javax.inject.Inject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderControllerTest extends Specification {

    def "parseOrder works"() {
        ApplicationContext.register(Formatter.class, new JacksonFormatter())
        ApplicationContext.register(OrderMessage.class, new OrderMessage())
        ApplicationContext.register(Order.class, new Order(null, "DogCow sent in a lab order", null))

        when:
        def parsedOrder = OrderController.getInstance().parseOrder("DogCow")

        then:
        parsedOrder == "{\"id\":\"missing id\"," +
                "\"destination\":\"DogCow sent in a lab order\"," +
                "\"createdAt\":\"missing timestamp\"}"
    }

    def "constructOrderMessage works"() {

        given:
        TestApplicationContext.register(Formatter.class, new JacksonFormatter())
        TestApplicationContext.register(OrderMessage.class, new OrderMessage())
        JacksonFormatter jackson = TestApplicationContext.getImplementation(Formatter)
        def orderController = OrderController.getInstance()
        def orderId = "1234abcd"
        def destination = "fake lab"
        LocalDateTime createAt = LocalDateTime.now(ZoneId.of("UTC"))
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
        def formattedDateTime = createAt.format(dateTimeFormat)
        TestApplicationContext.register(Order.class, new Order(orderId, destination, createAt))
        Order order = ApplicationContext.getImplementation(Order.class)

        def testing = " $orderId"
        def expected =
                "{\"id\":\"$orderId\"," +
                "\"destination\":\"$destination\"," +
                "\"createdAt\":\"$formattedDateTime\"}"

        println(expected)
        when:
        def orderMessage = orderController.constructOrderMessage(order)

        then:
        expected == orderMessage
    }
}
