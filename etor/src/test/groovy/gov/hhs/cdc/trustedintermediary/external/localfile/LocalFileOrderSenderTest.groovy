package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import java.nio.file.Files
import java.nio.file.Paths
import spock.lang.Specification

class LocalFileOrderSenderTest extends Specification{

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.register(LocalFileOrderSender, LocalFileOrderSender.getInstance())
    }

    def cleanup() {
        Files.deleteIfExists(Paths.get(LocalFileOrderSender.LOCAL_FILE_NAME))
    }

    def "send order works"() {

        given:
        def fhir = Mock(HapiFhir)

        def testStringOrder = "Some String"
        fhir.encodeResourceToJson(_ as String) >> testStringOrder

        def mockOrder = new OrderMock("ABC", null, "Mock String Order")

        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LocalFileOrderSender.getInstance().sendOrder(mockOrder)

        then:
        Files.readString(Paths.get(LocalFileOrderSender.LOCAL_FILE_NAME)) == testStringOrder
    }

    def "throws an exception when FHIR encoding does not work"() {

        given:
        def fhir = Mock(HapiFhir)
        def nullException = new NullPointerException()
        fhir.encodeResourceToJson(_ as String) >> {String argument -> throw nullException}

        def mockOrder = new OrderMock(null, null, "Second Mock String Order")

        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LocalFileOrderSender.getInstance().sendOrder(mockOrder)

        then:
        def exception = thrown(UnableToSendOrderException)
        exception.getCause() == nullException
    }

    def "log the step to metadata when send order is called"(){
        given:
        def fhir = Mock(HapiFhir)

        def testStringOrder = "Some String"
        fhir.encodeResourceToJson(_ as String) >> testStringOrder

        def mockOrder = new OrderMock("ABC", null, "Mock String Order")

        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        LocalFileOrderSender.getInstance().sendOrder(mockOrder)

        then:
        1 * LocalFileOrderSender.getInstance().metadata.put(_ as String, EtorMetadataStep.SENT_TO_REPORT_STREAM)
    }
}
