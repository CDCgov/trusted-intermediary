package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

class ConvertAndSendLabOrderUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ConvertAndSendLabOrderUsecase, ConvertAndSendLabOrderUsecase.getInstance())
    }

    def "ConvertAndSend"() {
        given:
        LabOrder<?> mockOrder = new LabOrder<String>() {
                    @Override
                    String getUnderlyingOrder() {
                        return "This is a mock inner order"
                    }
                }

        def mockConverter = Mock(LabOrderConverter)
        def mockSender = Mock(LabOrderSender)

        TestApplicationContext.register(LabOrderConverter, mockConverter)
        TestApplicationContext.register(LabOrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        def demographics = new PatientDemographics()

        when:
        ConvertAndSendLabOrderUsecase.getInstance().convertAndSend(demographics)

        then:
        1 * mockConverter.convertToOrder(_ as PatientDemographics) >> mockOrder
        1 * mockSender.sendOrder(mockOrder)
    }
}
