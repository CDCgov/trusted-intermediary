package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine
import gov.hhs.cdc.trustedintermediary.etor.utils.security.HashHelper
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

class SendOrderUseCaseTest extends Specification {

    def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
    def mockEngine = Mock(TransformationRuleEngine)
    def mockSender = Mock(OrderSender)
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendOrderUseCase, SendOrderUseCase.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)
        TestApplicationContext.register(SendMessageHelper, SendMessageHelper.getInstance())
        TestApplicationContext.register(TransformationRuleEngine, mockEngine)
        TestApplicationContext.register(OrderSender, mockSender)
        TestApplicationContext.register(HashHelper, HashHelper.getInstance())
        TestApplicationContext.register(Logger, mockLogger)
    }

    def "send sends successfully"() {
        given:
        def inboundReportId = "inboundReportId"
        def outboundReportId = "outboundReportId"
        def messagesIdsToLink = new HashSet<>(Set.of("messageId1", "messageId2"))
        def mockOrder = new OrderMock(null, null, null, null, null, null, null, null)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(mockOrder, inboundReportId)

        then:
        1 * mockEngine.runRules(mockOrder)
        1 * mockSender.send(mockOrder) >> Optional.of(outboundReportId)
        1 * mockOrchestrator.updateMetadataForInboundMessage(_ as PartnerMetadata)
        1 * mockOrchestrator.updateMetadataForOutboundMessage(inboundReportId, outboundReportId)
        1 * mockOrchestrator.findMessagesIdsToLink(inboundReportId) >> messagesIdsToLink
        1 * mockOrchestrator.linkMessages(messagesIdsToLink + inboundReportId)
    }

    def "send fails to send"() {
        given:
        mockSender.send(_) >> { throw new UnableToSendMessageException("DogCow", new NullPointerException()) }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(Mock(Order), _ as String)

        then:
        thrown(UnableToSendMessageException)
    }

    def "convertAndSend should log warnings for null inboundReportId"() {
        given:
        mockSender.send(_) >> Optional.of("outboundReportId")
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(Mock(Order), null)

        then:
        3 * mockLogger.logWarning(_)
        0 * mockOrchestrator.updateMetadataForInboundMessage(_, _)
    }

    def "convertAndSend logs error and continues when updateMetadataForReceivedOrder throws exception"() {
        given:
        def order = Mock(Order)
        def inboundReportId = "inboundReportId"

        mockOrchestrator.updateMetadataForInboundMessage(_ as PartnerMetadata) >> { throw new PartnerMetadataException("Error") }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(order, inboundReportId)

        then:
        1 * mockLogger.logError(_, _)
        1 * mockEngine.runRules(order)
        1 * mockOrchestrator.findMessagesIdsToLink(inboundReportId) >> Set.of()
        1 * mockSender.send(order) >> Optional.of("outboundReportId")
    }

    def "convertAndSend logs error and continues when updating the metadata for the sent order throws exception"() {
        given:
        def order = Mock(Order)
        def partnerMetadataException = new PartnerMetadataException("Error")
        mockOrchestrator.updateMetadataForOutboundMessage("inboundReportId", _) >> { throw  partnerMetadataException}
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(order, "inboundReportId")

        then:
        1 * mockEngine.runRules(order)
        1 * mockOrchestrator.findMessagesIdsToLink(_ as String) >> Set.of()
        1 * mockSender.send(order) >> Optional.of("outboundReportId")
        1 * mockLogger.logError(_, partnerMetadataException)
    }

    def "convertAndSend logs event when submissionId is null"() {
        given:
        def mockOrder = Mock(Order)
        TestApplicationContext.injectRegisteredImplementations()

        mockSender.send(_) >> Optional.empty()

        when:
        SendOrderUseCase.getInstance().convertAndSend(mockOrder, "inboundReportId")

        then:
        1 * mockLogger.logWarning(_)
        1 * mockOrchestrator.findMessagesIdsToLink(_ as String) >> Set.of()
        0 * mockOrchestrator.updateMetadataForOutboundMessage(_ as String, _ as String)
    }
}
