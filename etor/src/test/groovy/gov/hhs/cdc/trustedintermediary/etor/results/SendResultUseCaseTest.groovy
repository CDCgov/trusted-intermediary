package gov.hhs.cdc.trustedintermediary.etor.results

import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

class SendResultUseCaseTest extends Specification {

    def mockSender = Mock(ResultSender)
    def mockConverter = Mock(ResultConverter)
    def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendMessageUseCase, SendResultUseCase.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)
        TestApplicationContext.register(SendMessageHelper, SendMessageHelper.getInstance())
        TestApplicationContext.register(ResultConverter, mockConverter)
        TestApplicationContext.register(ResultSender, mockSender)
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "convertAndSend works"() {
        given:
        def mockResult = new ResultMock(null, "Mock result", null, null, null, null, null)
        def receivedSubmissionId = "receivedId"

        when:
        SendResultUseCase.getInstance().convertAndSend(mockResult, receivedSubmissionId)

        then:
        1 * mockConverter.addEtorProcessingTag(mockResult) >> mockResult
        1 * mockSender.send(mockResult) >> Optional.of("sentSubmissionId")
    }

    def "convertAndSend throws exception when send fails"() {
        given:
        def receivedSubmissionId = "receivedId"
        mockSender.send(_) >> { throw new UnableToSendMessageException("DogCow", new NullPointerException()) }

        when:
        SendResultUseCase.getInstance().convertAndSend(Mock(Result), receivedSubmissionId)

        then:
        thrown(UnableToSendMessageException)
    }

    def "convertAndSend logs error and continues when updateMetadataForReceivedMessage throws exception"() {
        given:
        def result = Mock(Result)
        def receivedSubmissionId = "receivedId"
        def messageType = PartnerMetadataMessageType.RESULT
        mockOrchestrator.updateMetadataForReceivedMessage(receivedSubmissionId, _ as String, messageType,result.getSendingApplicationDetails(),
                result.getSendingFacilityDetails(),
                result.getReceivingApplicationDetails(),
                result.getReceivingFacilityDetails(),
                result.getPlacerOrderNumber()) >> { throw new PartnerMetadataException("Error") }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(result, receivedSubmissionId)

        then:
        1 * mockLogger.logError(_, _)
        1 * mockConverter.addEtorProcessingTag(result) >> result
        1 * mockSender.send(result) >> Optional.of("sentId")
    }

    def "convertAndSend logs error and continues when updateMetadataForSentMessage throws exception"() {
        given:
        def result = Mock(Result)
        def receivedSubmissionId = "receivedId"
        mockOrchestrator.updateMetadataForSentMessage(receivedSubmissionId, _ as String) >> { throw new PartnerMetadataException("Error") }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(result, receivedSubmissionId)

        then:
        1 * mockConverter.addEtorProcessingTag(result) >> result
        1 * mockSender.send(result) >> Optional.of("sentId")
        1 * mockLogger.logError(_, _)
    }
}
