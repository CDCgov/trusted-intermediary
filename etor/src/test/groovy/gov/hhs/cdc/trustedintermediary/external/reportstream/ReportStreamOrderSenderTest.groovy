package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender
import gov.hhs.cdc.trustedintermediary.external.localfile.MockRSEndpointClient
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

class ReportStreamOrderSenderTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrderSender, ReportStreamOrderSender.getInstance())
        TestApplicationContext.register(RSEndpointClient, MockRSEndpointClient.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
    }

    def "send order works"() {
        given:
        def fhirResourceId = null
        def underlyingOrder = "Mock order"
        def mockOrder = new OrderMock(fhirResourceId, "patient-id", underlyingOrder, null, null, null, null, null)

        def senderHelper = Mock(ReportStreamSenderHelper)
        senderHelper.sendOrderToReportStream(underlyingOrder, fhirResourceId) >> Optional.of("fake-id")
        TestApplicationContext.register(ReportStreamSenderHelper, senderHelper)

        def mockFhir = Mock(HapiFhir)
        mockFhir.encodeResourceToJson(_ as String) >> underlyingOrder
        TestApplicationContext.register(HapiFhir, mockFhir)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().send(mockOrder)

        then:
        noExceptionThrown()
    }
}
