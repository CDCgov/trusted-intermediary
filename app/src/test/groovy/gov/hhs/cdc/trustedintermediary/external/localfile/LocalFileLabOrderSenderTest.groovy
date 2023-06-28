package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.LabOrdersMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
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

        def mockOrder = new LabOrdersMock(null, null, "Mock String Order")

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

        def mockOrder = new LabOrdersMock(null, null, "Second Mock String Order")

        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LocalFileLabOrderSender.getInstance().sendOrder(mockOrder)

        then:
        def exception = thrown(UnableToSendLabOrderException)
        exception.getCause() == nullException
    }
}
