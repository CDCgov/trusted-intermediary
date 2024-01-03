package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.localfile.LocalEndpointClient
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Cache
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

class ReportStreamOrderSenderTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrderSender, ReportStreamOrderSender.getInstance())
        TestApplicationContext.register(RSEndpointClient, LocalEndpointClient.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
    }

    def "send order works"() {
        given:
        def mockAuthEngine = Mock(AuthEngine)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)

        def mockSecrets = Mock(Secrets)
        TestApplicationContext.register(Secrets, mockSecrets)

        def mockClient = Mock(HttpClient)
        mockClient.post(_ as String, _ as Map, _ as String) >> """{"submissionId": "fake-id", "key": "value"}"""
        TestApplicationContext.register(HttpClient, mockClient)

        def mockFhir = Mock(HapiFhir)
        mockFhir.encodeResourceToJson(_ as String) >> "Mock order"
        TestApplicationContext.register(HapiFhir, mockFhir)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of("submissionId", "fake-id")
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockCache = Mock(Cache)
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().sendOrder(new OrderMock(null, null, "Mock order"))

        then:
        noExceptionThrown()
    }

    def "log the step to metadata when send order is called"() {
        given:

        def mockAuthEngine = Mock(AuthEngine)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)

        def mockSecrets = Mock(Secrets)
        TestApplicationContext.register(Secrets, mockSecrets)

        def mockClient = Mock(HttpClient)
        mockClient.post(_ as String, _ as Map, _ as String) >> """{"submissionId": "fake-id", "key": "value"}"""
        TestApplicationContext.register(HttpClient, mockClient)

        def mockFhir = Mock(HapiFhir)
        mockFhir.encodeResourceToJson(_ as String) >> "Mock order"
        TestApplicationContext.register(HapiFhir, mockFhir)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of("submissionId", "fake-id")
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockCache = Mock(Cache)
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().sendOrder(new OrderMock(null, null, "Mock order"))

        then:
        1 * ReportStreamOrderSender.getInstance().metadata.put(_, EtorMetadataStep.SENT_TO_REPORT_STREAM)
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
        def submissionId = ReportStreamOrderSender.getInstance().getSubmissionId(mockResponseBody)

        then:
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
        ReportStreamOrderSender.getInstance().getSubmissionId(mockResponseBody)

        then:
        1 * mockLogger.logError(_ as String, exception)
    }
}
