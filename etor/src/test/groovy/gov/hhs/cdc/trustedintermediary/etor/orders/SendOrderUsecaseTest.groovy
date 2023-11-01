package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData
import spock.lang.Specification

class SendOrderUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendOrderUseCase, SendOrderUseCase.getInstance())
        TestApplicationContext.register(MetricMetaData, Mock(MetricMetaData))
    }

    def "send sends successfully"() {
        given:
        def mockOrder = new OrderMock(null, null, null)
        def mockConverter = Mock(OrderConverter)
        def mockSender = Mock(OrderSender)

        TestApplicationContext.register(OrderConverter, mockConverter)
        TestApplicationContext.register(OrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(mockOrder)

        then:
        1 * mockConverter.convertMetadataToOmlOrder(mockOrder)
        1 * mockSender.sendOrder(_)
    }

    def "send fails to send"() {
        given:
        def mockOrder = new OrderMock(null, null, null)
        def mockConverter = Mock(OrderConverter)
        def mockSender = Mock(OrderSender)
        mockSender.sendOrder(_) >> { throw new UnableToSendOrderException("DogCow", new NullPointerException()) }

        TestApplicationContext.register(OrderConverter, mockConverter)
        TestApplicationContext.register(OrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(mockOrder)

        then:
        thrown(UnableToSendOrderException)
    }
}
