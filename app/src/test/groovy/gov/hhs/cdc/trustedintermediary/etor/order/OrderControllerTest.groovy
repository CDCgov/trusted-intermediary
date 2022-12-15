package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.JacksonFormatter
import spock.lang.Specification

import javax.inject.Inject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderControllerTest extends Specification {

    def "parseOrder works"() {
        when:
        def parsedOrder = OrderController.getInstance().parseOrder("DogCow")

        then:
        parsedOrder == "DogCow sent in a lab order"
    }

    def "constructOrderMessage works"() {

        given:
        TestApplicationContext.register(Formatter.class, new JacksonFormatter())
        JacksonFormatter jackson = TestApplicationContext.getImplementation(Formatter)
        def orderController = OrderController.getInstance()
        def orderId = "1234abcd"
        def destination = "fake lab"
        LocalDateTime createAt = LocalDateTime.now(ZoneId.of("UTC"))
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
        def formattedDateTime = createAt.format(dateTimeFormat)

        def testing = " $orderId"
        def expected =
                "order id: $orderId, destination: $destination, created at: $formattedDateTime"
        println(expected)
        when:
        def orderMessage = orderController.constructOrderMessage()

        then:
        expected == orderMessage
    }
}
