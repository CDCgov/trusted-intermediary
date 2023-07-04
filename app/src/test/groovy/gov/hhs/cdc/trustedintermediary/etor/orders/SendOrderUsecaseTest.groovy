package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

class SendOrderUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendOrderUseCase, SendOrderUseCase.getInstance())
    }

    def "send sends successfully"() {
        given:
        def mockOrder = new OrderMock(null, null, null)
        def mockSender = Mock(OrderSender)

        TestApplicationContext.register(OrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().send(mockOrder)

        then:
        1 * mockSender.sendOrder(mockOrder)
    }

    def "send fails to send"() {
        given:
        def mockOrder = new OrderMock(null, null, null)
        def mockSender = Mock(OrderSender)
        mockSender.sendOrder(_ as Order) >> { throw new UnableToSendOrderException("DogCow", new NullPointerException()) }

        TestApplicationContext.register(OrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().send(mockOrder)

        then:
        thrown(UnableToSendOrderException)
    }
}
