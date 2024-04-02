package gov.hhs.cdc.trustedintermediary.etor.messages

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class SendMessageHelperTest extends Specification {

    def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendMessageHelper, SendMessageHelper.getInstance())
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.injectRegisteredImplementations()
    }
    def "savePartnerMetadataForReceivedMessage works"() {
        when:
        SendMessageHelper.getInstance().savePartnerMetadataForReceivedMessage("receivedId", new Random().nextInt(), PartnerMetadataMessageType.RESULT,"sending_app", "sending_facility", "receiving_app", "receiving_facility", "placer_order_number")

        then:
        1 * mockOrchestrator.updateMetadataForReceivedMessage(_, _, _, _, _, _, _, _)
    }

    def "savePartnerMetadataForReceivedMessage should log warnings for null receivedSubmissionId"() {
        when:
        SendMessageHelper.getInstance().savePartnerMetadataForReceivedMessage(null, new Random().nextInt(), PartnerMetadataMessageType.RESULT, "sending_app", "sending_facility", "receiving_app", "receiving_facility", "placer_order_number")

        then:
        1 * mockLogger.logWarning(_)
    }

    def "savePartnerMetadataForReceivedMessage logs error and continues when updateMetadataForReceivedMessage throws error"() {
        given:
        def hashCode = new Random().nextInt()
        def messageType = PartnerMetadataMessageType.RESULT
        def receivedSubmissionId = "receivedId"
        def sendingApp = "sending_app"
        def sendingFacility = "sending_facility"
        def receivingApp = "receiving_app"
        def receivingFacility = "receiving_facility"
        def placerOrderNumber = "placer_order_number"
        mockOrchestrator.updateMetadataForReceivedMessage(receivedSubmissionId, _ as String, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber) >> { throw new PartnerMetadataException("Error") }

        when:
        SendMessageHelper.getInstance().savePartnerMetadataForReceivedMessage(receivedSubmissionId, hashCode, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        then:
        1 * mockLogger.logError(_, _)
    }

    def "saveSentMessageSubmissionId works"() {
        given:
        def sentSubmissionId = "sentId"
        def receivedSubmissionId = "receivedId"
        mockOrchestrator.updateMetadataForSentMessage(receivedSubmissionId, _ as String) >> { throw new PartnerMetadataException("Error") }

        when:
        SendMessageHelper.getInstance().saveSentMessageSubmissionId(receivedSubmissionId, sentSubmissionId)

        then:
        1 * mockOrchestrator.updateMetadataForSentMessage(_, _)
    }

    def "saveSentMessageSubmissionId should log warnings for null receivedSubmissionId"() {
        given:
        def receivedSubmissionId = "receivedId"

        when:
        SendMessageHelper.getInstance().saveSentMessageSubmissionId(null, receivedSubmissionId)

        then:
        1 * mockLogger.logWarning(_)
    }

    def "saveSentMessageSubmissionId should log error and continues when updateMetadataForSentMessage throws error"() {
        given:
        def sentSubmissionId = "sentId"
        def receivedSubmissionId = "receivedId"
        mockOrchestrator.updateMetadataForSentMessage(receivedSubmissionId, _ as String) >> { throw new PartnerMetadataException("Error") }

        when:
        SendMessageHelper.getInstance().saveSentMessageSubmissionId(receivedSubmissionId, sentSubmissionId)

        then:
        1 * mockLogger.logError(_, _)
    }
}
