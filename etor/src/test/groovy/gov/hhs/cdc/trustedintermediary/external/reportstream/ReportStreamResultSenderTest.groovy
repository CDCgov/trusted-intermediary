package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.results.ResultSender
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import spock.lang.Specification

class ReportStreamResultSenderTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ResultSender, ReportStreamResultSender.getInstance())
    }

    def "send results works"() {
        given:
        def fhirResourceId = null
        def underlyingResult = "Mock result"
        def mockResult = new ResultMock(fhirResourceId, underlyingResult)

        def senderHelper = Mock(ReportStreamSenderHelper)
        senderHelper.sendResultToReportStream(underlyingResult, fhirResourceId) >> Optional.of("fake-id")
        TestApplicationContext.register(ReportStreamSenderHelper, senderHelper)

        def mockFhir = Mock(HapiFhir)
        mockFhir.encodeResourceToJson(_ as String) >> underlyingResult
        TestApplicationContext.register(HapiFhir, mockFhir)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamResultSender.getInstance().send(mockResult)

        then:
        noExceptionThrown()
    }
}
