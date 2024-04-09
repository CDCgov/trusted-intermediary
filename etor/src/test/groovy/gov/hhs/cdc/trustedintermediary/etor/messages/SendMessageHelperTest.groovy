package gov.hhs.cdc.trustedintermediary.etor.messages

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class SendMessageHelperTest extends Specification {
    def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
    def mockLogger = Mock(Logger)
    private sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
    private sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
    private receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
    private receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
    private placerOrderNumber = "placer_order_number"

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
        SendMessageHelper.getInstance().savePartnerMetadataForReceivedMessage("receivedId", new Random().nextInt(), PartnerMetadataMessageType.RESULT,sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        then:
        1 * mockOrchestrator.updateMetadataForReceivedMessage(_, _, _, _, _, _, _, _)
    }

    def "savePartnerMetadataForReceivedMessage should log warnings for null receivedSubmissionId"() {
        when:
        SendMessageHelper.getInstance().savePartnerMetadataForReceivedMessage(null, new Random().nextInt(), PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        then:
        1 * mockLogger.logWarning(_)
    }

    def "savePartnerMetadataForReceivedMessage logs error and continues when updateMetadataForReceivedMessage throws error"() {
        given:
        def hashCode = new Random().nextInt()
        def messageType = PartnerMetadataMessageType.RESULT
        def receivedSubmissionId = "receivedId"
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

    def "linkMessage logs warning and ends silently when passed a null id"() {
        when:
        SendMessageHelper.getInstance().linkMessage(null)

        then:
        1 * mockLogger.logWarning(_, _)
        notThrown(Exception)
    }

    def "linkMessage logs error when there's a PartnerMetadataException"() {
        given:
        mockOrchestrator.findMessagesIdsToLink(_ as String) >> {throw new PartnerMetadataException("")}

        when:
        SendMessageHelper.getInstance().linkMessage("1")

        then:
        1 * mockLogger.logError(_, _)
        notThrown(PartnerMetadataException)
    }

    def "linkMessage logs error when there's a MessageLinkException"() {
        given:
        mockOrchestrator.findMessagesIdsToLink(_ as String) >> ["1"]
        mockOrchestrator.linkMessages(_ as Set<String>) >> {throw new MessageLinkException("")}

        when:
        SendMessageHelper.getInstance().linkMessage("1")

        then:
        1 * mockLogger.logError(_, _)
        notThrown(MessageLinkException)
    }

    def "linkMessage finishes silently if the list of message ids is null"() {
        given:
        mockOrchestrator.findMessagesIdsToLink(_ as String) >> null

        when:
        SendMessageHelper.getInstance().linkMessage("1")

        then:
        0 * mockLogger.logWarning(_, _)
        0 * mockLogger.logError(_, _)
        notThrown(Exception)
    }

    def "linkMessage finishes silently if the list of message ids is empty"() {
        given:
        mockOrchestrator.findMessagesIdsToLink(_ as String) >> []

        when:
        SendMessageHelper.getInstance().linkMessage("1")

        then:
        0 * mockLogger.logWarning(_, _)
        0 * mockLogger.logError(_, _)
        notThrown(Exception)
    }
}
