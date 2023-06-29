package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.LabOrdersMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

class SendLabOrderUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendLabOrderUsecase, SendLabOrderUsecase.getInstance())
    }

    def "send sends successfully"() {
        given:
        def mockOrder = new LabOrdersMock(null, null, null)
        def mockSender = Mock(LabOrderSender)

        TestApplicationContext.register(LabOrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendLabOrderUsecase.getInstance().send(mockOrder)

        then:
        1 * mockSender.sendOrder(mockOrder)
    }

    def "send fails to send"() {
        given:
        def mockOrder = new LabOrdersMock(null, null, null)
        def mockSender = Mock(LabOrderSender)
        mockSender.sendOrder(_ as LabOrder) >> { throw new UnableToSendLabOrderException("DogCow", new NullPointerException()) }

        TestApplicationContext.register(LabOrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendLabOrderUsecase.getInstance().send(mockOrder)

        then:
        thrown(UnableToSendLabOrderException)
    }
}
