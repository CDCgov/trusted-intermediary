package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder
import spock.lang.Specification

class ReportStreamLabOrderSenderTest extends Specification{

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ReportStreamLabOrderSender, ReportStreamLabOrderSender.getInstance())
    }

    def "send order works"() {

        given:
        LabOrder<?> mockOrder = new LabOrder<String>() {

                    @Override
                    String getUnderlyingOrder() {
                        return "Mock order"
                    }
                }

        when:

        ReportStreamLabOrderSender.getInstance().sendOrder(mockOrder)

        then:

        noExceptionThrown()
    }
}
