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
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "convertAndSend works"() {
        given:
        def mockResult = new ResultMock(null, "Mock result", null, null, null, null, null)
        def outboundMessageId = "receivedId"

        when:
        SendResultUseCase.getInstance().convertAndSend(mockResult, outboundMessageId)

        then:
        1 * mockEngine.runRules(mockResult)
        1 * mockSender.send(mockResult) >> Optional.of("inboundMessageId")
    }

    def "convertAndSend throws exception when send fails"() {
        given:
        def outboundMessageId = "receivedId"
        mockSender.send(_) >> { throw new UnableToSendMessageException("DogCow", new NullPointerException()) }

        when:
        SendResultUseCase.getInstance().convertAndSend(Mock(Result), outboundMessageId)

        then:
        thrown(UnableToSendMessageException)
    }

    def "convertAndSend logs error and continues when updateMetadataForReceivedMessage throws exception"() {
        given:
        def result = Mock(Result)
        def outboundMessageId = "receivedId"
        mockOrchestrator.updateMetadataForReceivedMessage(_ as PartnerMetadata) >> { throw new PartnerMetadataException("Error") }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(result, outboundMessageId)

        then:
        1 * mockLogger.logError(_, _)
        1 * mockEngine.runRules(result)
        1 * mockSender.send(result) >> Optional.of("sentId")
    }

    def "convertAndSend logs error and continues when updateMetadataForSentMessage throws exception"() {
        given:
        def result = Mock(Result)
        def outboundMessageId = "receivedId"
        mockOrchestrator.updateMetadataForSentMessage(outboundMessageId, _ as String) >> { throw new PartnerMetadataException("Error") }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(result, outboundMessageId)

        then:
        1 * mockEngine.runRules(result)
        1 * mockSender.send(result) >> Optional.of("sentId")
        1 * mockLogger.logError(_, _)
    }
}
