package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrder
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendLabOrderException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class LocalFileLabOrderSenderTest extends Specification{

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(LocalFileLabOrderSender, LocalFileLabOrderSender.getInstance())
    }

    def "send order works"() {

        given:
        def fhir = Mock(HapiFhir)

        def testStringOrder = "Some String"
        fhir.encodeResourceToJson(_ as String) >> testStringOrder

        LabOrder<?> mockOrder = new LabOrder<String>() {

                    @Override
                    String getUnderlyingOrder() {
                        return "Mock String Order"
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

        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LocalFileLabOrderSender.getInstance().sendOrder(mockOrder)

        then:
        Files.readString(Paths.get(LocalFileLabOrderSender.LOCAL_FILE_NAME)) == testStringOrder
    }

    def "throws an exception when FHIR encoding does not work"() {

        given:
        def fhir = Mock(HapiFhir)
        def nullException = new NullPointerException()
        fhir.encodeResourceToJson(_ as String) >> {String argument -> throw nullException}

        LabOrder<?> mockOrder = new LabOrder<String>() {

                    @Override
                    String getUnderlyingOrder() {
                        return " Second Mock String Order"
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

        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LocalFileLabOrderSender.getInstance().sendOrder(mockOrder)

        then:
        def exception = thrown(UnableToSendLabOrderException)
        exception.getCause() == nullException
    }
}
