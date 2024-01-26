package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageSender

import spock.lang.Specification

class ConvertAndSendDemographicsUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ConvertAndSendDemographicsUsecase, ConvertAndSendDemographicsUsecase.getInstance())
    }

    def "ConvertAndSend"() {
        given:
        def mockOrder = new OrderMock(null, null, null)
        def mockConverter = Mock(OrderConverter)
        def mockSender = Mock(MessageSender)

        TestApplicationContext.register(OrderConverter, mockConverter)
        TestApplicationContext.register(MessageSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        def demographics = new DemographicsMock(null, null, null)

        when:
        ConvertAndSendDemographicsUsecase.getInstance().convertAndSend(demographics)

        then:
        1 * mockConverter.convertToOrder(_ as Demographics) >> mockOrder
        1 * mockSender.send(mockOrder)
    }
}
