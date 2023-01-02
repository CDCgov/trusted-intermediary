package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.JacksonFormatter
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrderController, OrderController.getInstance())
    }

    def "parseOrder works"() {
        given:
        def mockOrderId = "asdf-12341-jkl-7890"

        def formatter = Mock(JacksonFormatter)
        formatter.convertToObject(_ as String, _ as Class) >> new Order(mockOrderId, "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow")

        def request = new DomainRequest()

        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def parsedOrder = OrderController.getInstance().parseOrder(request)

        then:
        noExceptionThrown()
        parsedOrder.getId() == mockOrderId
    }

    def "constructOrderMessage works"() {

        given:
        def orderId = "1234abcd"
        def destination = "fake lab"
        def client = "fake client"
        def content = "MSH|lab order"
        LocalDateTime createAt = LocalDateTime.now(ZoneId.of("UTC"))
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
        def formattedDateTime = createAt.format(dateTimeFormat)
        Order order = new Order(orderId, destination, createAt.format(dateTimeFormat), client, content)
        def orderController = OrderController.getInstance()

        def expected = """\"{\\"id\\":\\"1234abcd\\",\\"destination\\":\\"fake lab\\",\\"createdAt\\":\\"$formattedDateTime\\",\\"client\\":\\"fake client\\",\\"content\\":\\"MSH|lab order\\"}\""""


        when:
        def orderMessage = orderController.constructResponse(order)

        then:
        expected == orderMessage
    }
}
