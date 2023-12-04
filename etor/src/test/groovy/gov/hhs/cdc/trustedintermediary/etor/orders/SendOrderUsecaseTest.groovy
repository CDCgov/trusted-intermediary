package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetaDataStep
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData
import spock.lang.Specification

class SendOrderUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendOrderUseCase, SendOrderUseCase.getInstance())
        TestApplicationContext.register(MetricMetaData, Mock(MetricMetaData))
        TestApplicationContext.register(PartnerMetadataStorage, Mock(PartnerMetadataStorage))
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

    def "metadata is registered for converting to OML"() {
        given:
        TestApplicationContext.register(OrderConverter, Mock(OrderConverter))
        TestApplicationContext.register(OrderSender, Mock(OrderSender))
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(new OrderMock(null, null, null))

        then:
        1 * SendOrderUseCase.getInstance().metaData.put(_, EtorMetaDataStep.ORDER_CONVERTED_TO_OML)
    }

    def "metadata is registered for adding the contact section to an order"() {
        given:
        TestApplicationContext.register(OrderConverter, Mock(OrderConverter))
        TestApplicationContext.register(OrderSender, Mock(OrderSender))
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(new OrderMock(null, null, null))

        then:
        1 * SendOrderUseCase.getInstance().metaData.put(_, EtorMetaDataStep.CONTACT_SECTION_ADDED_TO_PATIENT)
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
