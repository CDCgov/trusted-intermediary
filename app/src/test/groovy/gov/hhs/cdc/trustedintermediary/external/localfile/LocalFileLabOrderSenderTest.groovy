package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder
import spock.lang.Specification

class LocalFileLabOrderSenderTest extends Specification{


    def "send order works"() {

        given:
        LabOrder<?> mockOrder = new LabOrder<String>() {

                    @Override
                    String getUnderlyingOrder() {
                        return "Mock order"
                    }
                }

        when:

        LocalFileLabOrderSender.getInstance().sendOrder(mockOrder)

        then:

        noExceptionThrown()
    }
}
