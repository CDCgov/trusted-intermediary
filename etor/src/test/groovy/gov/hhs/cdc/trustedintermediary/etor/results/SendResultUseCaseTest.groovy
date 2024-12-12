package gov.hhs.cdc.trustedintermediary.etor.results

import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine
import gov.hhs.cdc.trustedintermediary.etor.utils.security.HashHelper
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

class SendResultUseCaseTest extends Specification {

    def mockSender = Mock(ResultSender)
    def mockEngine = Mock(TransformationRuleEngine)
    def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendMessageUseCase, SendResultUseCase.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)
        TestApplicationContext.register(SendMessageHelper, SendMessageHelper.getInstance())
        TestApplicationContext.register(TransformationRuleEngine, mockEngine)
        TestApplicationContext.register(ResultSender, mockSender)
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(HashHelper, HashHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "convertAndSend works"() {
        given:
        def mockResult = new ResultMock(null, "Mock result", null, null, null, null, null)
        def inboundReportId = "inboundReportId"

        when:
        SendResultUseCase.getInstance().convertAndSend(mockResult, inboundReportId)

        then:
        1 * mockEngine.runRules(mockResult)
        1 * mockSender.send(mockResult) >> Optional.of("outboundReportId")
    }

    def "convertAndSend throws exception when send fails"() {
        given:
        def inboundReportId = "inboundReportId"
        mockSender.send(_) >> { throw new UnableToSendMessageException("DogCow", new NullPointerException()) }

        when:
        SendResultUseCase.getInstance().convertAndSend(Mock(Result), inboundReportId)

        then:
        thrown(UnableToSendMessageException)
    }

    def "convertAndSend logs error and continues when updateMetadataForInboundMessage throws exception"() {
        given:
        def result = Mock(Result)
        def inboundReportId = "inboundReportId"
        mockOrchestrator.updateMetadataForInboundMessage(_ as PartnerMetadata) >> { throw new PartnerMetadataException("Error") }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(result, inboundReportId)

        then:
        1 * mockLogger.logError(_, _)
        1 * mockEngine.runRules(result)
        1 * mockSender.send(result) >> Optional.of("outboundReportId")
    }

    def "convertAndSend logs error and continues when updateMetadataForOutboundMessage throws exception"() {
        given:
        def result = Mock(Result)
        def inboundReportId = "inboundReportId"
        mockOrchestrator.updateMetadataForOutboundMessage(inboundReportId, _ as String) >> { throw new PartnerMetadataException("Error") }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(result, inboundReportId)

        then:
        1 * mockEngine.runRules(result)
        1 * mockSender.send(result) >> Optional.of("outboundReportId")
        1 * mockLogger.logError(_, _)
    }
}
