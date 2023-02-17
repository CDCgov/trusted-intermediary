package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class LocalFileLabOrderSenderTest extends Specification{


    def "send order works"() {

        given:
        LabOrder<?> mockOrder = new LabOrder<Bundle>() {

                    @Override
                    Bundle getUnderlyingOrder() {
                        return new Bundle()
                    }
                }

        when:

        LocalFileLabOrderSender.getInstance().sendOrder(mockOrder)

        then:

        noExceptionThrown()
    }
}
