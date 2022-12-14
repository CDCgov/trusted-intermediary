package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.JacksonFormatter
import spock.lang.Specification

import javax.inject.Inject

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
        def expected = ""

        when:
        def orderMessage = orderController.constructOrderMessage()

        then:
        expected == orderMessage
    }
}
