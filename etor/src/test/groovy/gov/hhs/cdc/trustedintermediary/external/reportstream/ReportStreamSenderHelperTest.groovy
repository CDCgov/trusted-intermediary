package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.localfile.MockRSEndpointClient
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

class ReportStreamSenderHelperTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ReportStreamSenderHelper, ReportStreamSenderHelper.getInstance())
        TestApplicationContext.register(RSEndpointClient, MockRSEndpointClient.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
    }

    def "sendToReportStream works"() {
        given:
        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of('submissionId', 'fake-id')
        TestApplicationContext.register(Formatter, mockFormatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamSenderHelper.getInstance().sendToReportStream(_ as String, _ as String, _ as String)

        then:
        noExceptionThrown()
        1 * ReportStreamSenderHelper.getInstance().metadata.put(_, EtorMetadataStep.SENT_TO_REPORT_STREAM)
    }

    def "sendOrderToReportStream works"() {
        setup:
        def body = "testBody"
        def fhirResourceId = "testId"
        def expected = Optional.of("result")

        def senderHelper = Spy(ReportStreamSenderHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def order = senderHelper.sendOrderToReportStream(body, fhirResourceId)

        then:
        order.get() == expected.get()
        1 * senderHelper.sendToReportStream(body, fhirResourceId, "order") >> expected
    }

    def "sendResultToReportStream works"() {
        setup:
        def body = "testBody"
        def fhirResourceId = "testId"
        def expected = Optional.of("result")

        def senderHelper = Spy(ReportStreamSenderHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def result = senderHelper.sendResultToReportStream(body, fhirResourceId)

        then:
        result.get() == expected.get()
        1 * senderHelper.sendToReportStream(body, fhirResourceId, "result") >> expected
    }

    def "sendToReportStream throws exception if RS client fails"() {
        setup:
        def mockEndpointClient = Mock(RSEndpointClient)
        mockEndpointClient.getRsToken() >> { throw new ReportStreamEndpointClientException("couldn't get token", new Exception())}
        TestApplicationContext.register(RSEndpointClient, mockEndpointClient)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamSenderHelper.getInstance().sendToReportStream("testBody", "testId", "testType")

        then:
        thrown(UnableToSendMessageException)
    }

    def "getSubmissionId logs submissionId if convertJsonToObject is successful"() {
        given:
        def mockSubmissionId = "fake-id"
        def mockResponseBody = """{"submissionId": "${mockSubmissionId}", "key": "value"}"""

        TestApplicationContext.register(Formatter, Jackson.getInstance())

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def submissionId = ReportStreamSenderHelper.getInstance().getSubmissionId(mockResponseBody)

        then:
        noExceptionThrown()
        submissionId.get() == mockSubmissionId
    }

    def "getSubmissionId logs error if convertJsonToObject fails"() {
        given:
        def mockResponseBody = '{"submissionId": "fake-id", "key": "value"}'
        def exception = new FormatterProcessingException("couldn't convert json", new Exception())

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> { throw exception }
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamSenderHelper.getInstance().getSubmissionId(mockResponseBody)

        then:
        1 * mockLogger.logError(_ as String, exception)
    }
}
