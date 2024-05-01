package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.Order
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiMessageHelper
import spock.lang.Specification

class ConvertAndSendDemographicsUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiMessageHelper, HapiMessageHelper.getInstance())
        TestApplicationContext.register(ConvertAndSendDemographicsUsecase, ConvertAndSendDemographicsUsecase.getInstance())
    }

    def "ConvertAndSend"() {
        given:
        def mockEngine = Mock(TransformationRuleEngine)
        def mockSender = Mock(OrderSender)

        TestApplicationContext.register(TransformationRuleEngine, mockEngine)
        TestApplicationContext.register(OrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        def demographics = new DemographicsMock(null, null, null)

        when:
        ConvertAndSendDemographicsUsecase.getInstance().convertAndSend(demographics)

        then:
        1 * mockEngine.runRules(_ as Demographics)
        1 * mockSender.send(_ as Order)
    }
}
