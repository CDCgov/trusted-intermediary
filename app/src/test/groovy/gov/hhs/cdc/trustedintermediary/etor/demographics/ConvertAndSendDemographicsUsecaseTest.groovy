package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.LabOrdersMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrder
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrderConverter
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrderSender
import spock.lang.Specification

class ConvertAndSendDemographicsUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ConvertAndSendDemographicsUsecase, ConvertAndSendDemographicsUsecase.getInstance())
    }

    def "ConvertAndSend"() {
        given:
        def mockOrder = new LabOrdersMock(null, null, null)
        def mockConverter = Mock(LabOrderConverter)
        def mockSender = Mock(LabOrderSender)

        TestApplicationContext.register(LabOrderConverter, mockConverter)
        TestApplicationContext.register(LabOrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        def demographics = new DemographicsMock(null, null, null)

        when:
        ConvertAndSendDemographicsUsecase.getInstance().convertAndSend(demographics)

        then:
        1 * mockConverter.convertToOrder(_ as Demographics) >> mockOrder
        1 * mockSender.sendOrder(mockOrder)
    }
}
