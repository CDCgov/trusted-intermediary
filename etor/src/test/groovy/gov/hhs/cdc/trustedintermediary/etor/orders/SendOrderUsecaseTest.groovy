package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

class SendOrderUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendOrderUseCase, SendOrderUseCase.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.register(PartnerMetadataOrchestrator, Mock(PartnerMetadataOrchestrator))
    }

    def "send sends successfully"() {
        given:
        def mockOrder = new OrderMock(null, null, null)

        def mockConverter = Mock(OrderConverter)
        TestApplicationContext.register(OrderConverter, mockConverter)

        def mockSender = Mock(OrderSender)
        TestApplicationContext.register(OrderSender, mockSender)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(mockOrder, _ as String)

        then:
        1 * mockConverter.convertMetadataToOmlOrder(mockOrder)
        1 * mockConverter.addContactSectionToPatientResource(_)
        1 * mockSender.sendOrder(_) >> Optional.empty()
    }

    def "metadata is registered for converting to OML and for adding the contact section to an order"() {
        given:
        TestApplicationContext.register(OrderConverter, Mock(OrderConverter))

        def mockSender = Mock(OrderSender)
        mockSender.sendOrder(_) >> Optional.empty()
        TestApplicationContext.register(OrderSender, mockSender)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(new OrderMock(null, null, null), _ as String)

        then:
        1 * SendOrderUseCase.getInstance().metadata.put(_, EtorMetadataStep.ORDER_CONVERTED_TO_OML)
        1 * SendOrderUseCase.getInstance().metadata.put(_, EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT)
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
        SendOrderUseCase.getInstance().convertAndSend(mockOrder, _ as String)

        then:
        thrown(UnableToSendOrderException)
    }
}
