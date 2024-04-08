package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

class SendOrderUseCaseTest extends Specification {

    def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
    def mockConverter = Mock(OrderConverter)
    def mockSender = Mock(OrderSender)
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendOrderUseCase, SendOrderUseCase.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)
        TestApplicationContext.register(SendMessageHelper, SendMessageHelper.getInstance())
        TestApplicationContext.register(OrderConverter, mockConverter)
        TestApplicationContext.register(OrderSender, mockSender)
        TestApplicationContext.register(Logger, mockLogger)
    }

    def "send sends successfully"() {
        given:
        def receivedSubmissionId = "receivedId"
        def sentSubmissionId = "sentId"
        def messageType = PartnerMetadataMessageType.ORDER

        def sendOrder = SendOrderUseCase.getInstance()
        def mockOrder = new OrderMock(null, null, null, null, null, null, null, null)
        def mockOmlOrder = Mock(Order)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        sendOrder.convertAndSend(mockOrder, receivedSubmissionId)

        then:
        1 * mockConverter.convertToOmlOrder(mockOrder) >> mockOmlOrder
        1 * mockConverter.addContactSectionToPatientResource(mockOmlOrder) >> mockOmlOrder
        1 * mockConverter.addEtorProcessingTag(mockOmlOrder) >> mockOmlOrder
        1 * mockSender.send(mockOmlOrder) >> Optional.of(sentSubmissionId)
        1 * sendOrder.metadata.put(_, EtorMetadataStep.ORDER_CONVERTED_TO_OML)
        1 * sendOrder.metadata.put(_, EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT)
        1 * sendOrder.metadata.put(_, EtorMetadataStep.ETOR_PROCESSING_TAG_ADDED_TO_MESSAGE_HEADER)
        1 * mockOrchestrator.updateMetadataForReceivedMessage(
                receivedSubmissionId,
                _ as String,
                messageType,
                mockOrder.getSendingApplicationDetails(),
                mockOrder.getSendingFacilityDetails(),
                mockOrder.getReceivingApplicationDetails(),
                mockOrder.getReceivingFacilityDetails(),
                mockOrder.getPlacerOrderNumber())
        1 * mockOrchestrator.updateMetadataForSentMessage(receivedSubmissionId, sentSubmissionId)
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

    def "convertAndSend should log warnings for null receivedSubmissionId"() {
        given:
        mockSender.send(_) >> Optional.of("sentSubmissionId")
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(Mock(Order), null)

        then:
        2 * mockLogger.logWarning(_)
        0 * mockOrchestrator.updateMetadataForReceivedMessage(_, _)
    }

    def "convertAndSend logs error and continues when updateMetadataForReceivedOrder throws exception"() {
        given:
        def order = Mock(Order)
        def omlOrder = Mock(Order)
        def receivedSubmissionId = "receivedId"
        def messageType = PartnerMetadataMessageType.ORDER
        mockOrchestrator.updateMetadataForReceivedMessage(receivedSubmissionId, _ as String, messageType,
                order.getSendingApplicationDetails(),
                order.getSendingFacilityDetails(),
                order.getReceivingApplicationDetails(),
                order.getReceivingFacilityDetails(),
                order.getPlacerOrderNumber()) >> { throw new PartnerMetadataException("Error") }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(order, receivedSubmissionId)

        then:
        1 * mockLogger.logError(_, _)
        1 * mockConverter.convertToOmlOrder(order) >> omlOrder
        1 * mockConverter.addContactSectionToPatientResource(omlOrder) >> omlOrder
        1 * mockConverter.addEtorProcessingTag(omlOrder) >> omlOrder
        1 * mockSender.send(omlOrder) >> Optional.of("sentId")
    }

    def "convertAndSend logs error and continues when updating the metadata for the sent order throws exception"() {
        given:
        def order = Mock(Order)
        def omlOrder = Mock(Order)
        def partnerMetadataException = new PartnerMetadataException("Error")
        mockOrchestrator.updateMetadataForSentMessage("receivedId", _) >> { throw  partnerMetadataException}
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(order, "receivedId")

        then:
        1 * mockConverter.convertToOmlOrder(order) >> omlOrder
        1 * mockConverter.addContactSectionToPatientResource(omlOrder) >> omlOrder
        1 * mockConverter.addEtorProcessingTag(omlOrder) >> omlOrder
        1 * mockSender.send(omlOrder) >> Optional.of("sentId")
        1 * mockLogger.logError(_, partnerMetadataException)
    }

    def "convertAndSend logs event when submissionId is null"() {
        given:
        def mockOrder = Mock(Order)
        TestApplicationContext.injectRegisteredImplementations()

        mockSender.send(_) >> Optional.empty()

        when:
        SendOrderUseCase.getInstance().convertAndSend(mockOrder, "receivedId")

        then:
        1 * mockLogger.logWarning(_)
        0 * mockOrchestrator.updateMetadataForSentMessage(_ as String, _ as String)
    }
}
