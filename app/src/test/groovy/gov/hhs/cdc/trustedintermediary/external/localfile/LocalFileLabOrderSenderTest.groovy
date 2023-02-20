package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class LocalFileLabOrderSenderTest extends Specification{

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        // TestApplicationContext.register(HapiFhir, HapiFhirImplementation.getInstance())
        TestApplicationContext.register(LocalFileLabOrderSender, LocalFileLabOrderSender.getInstance())
        // TestApplicationContext.injectRegisteredImplementations()
    }

    def "send order works"() {

        given:
        def fhir = Mock(HapiFhir)
        LabOrder<?> mockOrder = new LabOrder<Bundle>() {

                    @Override
                    Bundle getUnderlyingOrder() {
                        return new Bundle()
                    }
                }

        TestApplicationContext.register(HapiFhir, HapiFhirImplementation.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LocalFileLabOrderSender.getInstance().sendOrder(mockOrder)

        then:

        noExceptionThrown()
    }
}
