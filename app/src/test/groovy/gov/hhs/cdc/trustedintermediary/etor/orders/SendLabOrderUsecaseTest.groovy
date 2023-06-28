package gov.hhs.cdc.trustedintermediary.etor.orders


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
        LabOrder<?> mockOrder = new LabOrder<String>() {
                    @Override
                    String getUnderlyingOrder() {
                        return "This is a mock inner order"
                    }

                    @Override
                    String getFhirResourceId() {
                        return null
                    }

                    @Override
                    String getPatientId() {
                        return null
                    }
                }

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
        LabOrder<?> mockOrder = new LabOrder<String>() {
                    @Override
                    String getUnderlyingOrder() {
                        return "This is a mock inner order"
                    }

                    @Override
                    String getFhirResourceId() {
                        return null
                    }

                    @Override
                    String getPatientId() {
                        return null
                    }
                }

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
