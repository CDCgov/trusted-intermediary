package gov.hhs.cdc.trustedintermediary.etor.messages

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
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
    private partnerMetadata

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendMessageHelper, SendMessageHelper.getInstance())
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.injectRegisteredImplementations()
        partnerMetadata = new PartnerMetadata(
                "outboundId",
                new Random().nextInt().toString(),
                PartnerMetadataMessageType.RESULT,
                sendingApp,
                sendingFacility,
                receivingApp,
                receivingFacility,
                placerOrderNumber)
    }
    def "savePartnerMetadataForOutboundMessage works"() {
        when:
        SendMessageHelper.getInstance().savePartnerMetadataForOutboundMessage(partnerMetadata)

        then:
        1 * mockOrchestrator.updateMetadataForOutboundMessage(_)
    }

    def "savePartnerMetadataForOutboundMessage should log warnings for null outboundMessageId"() {
        when:
        PartnerMetadata warningPartnerMetadata = new PartnerMetadata(
                null,
                new Random().nextInt().toString(),
                PartnerMetadataMessageType.RESULT,
                sendingApp,
                sendingFacility,
                receivingApp,
                receivingFacility,
                placerOrderNumber)
        SendMessageHelper.getInstance().savePartnerMetadataForOutboundMessage(warningPartnerMetadata)

        then:
        1 * mockLogger.logWarning(_)
    }

    def "savePartnerMetadataForOutboundMessage logs error and continues when updateMetadataForOutboundMessage throws error"() {
        given:
        def hashCode = new Random().nextInt()
        def messageType = PartnerMetadataMessageType.RESULT
        mockOrchestrator.updateMetadataForOutboundMessage(partnerMetadata) >> { throw new PartnerMetadataException("Error") }

        when:
        SendMessageHelper.getInstance().savePartnerMetadataForOutboundMessage(partnerMetadata)

        then:
        1 * mockLogger.logError(_, _)
    }

    def "saveInboundMessageId works"() {
        given:
        def inboundMessageId = "inboundId"
        def outboundMessageId = "outboundId"
        mockOrchestrator.updateMetadataForInboundMessage(outboundMessageId, _ as String) >> { throw new PartnerMetadataException("Error") }

        when:
        SendMessageHelper.getInstance().saveInboundMessageId(outboundMessageId, inboundMessageId)

        then:
        1 * mockOrchestrator.updateMetadataForInboundMessage(_, _)
    }

    def "saveInboundMessageId should log warnings for null outboundMessageId"() {
        given:
        def outboundMessageId = "outboundId"

        when:
        SendMessageHelper.getInstance().saveInboundMessageId(null, outboundMessageId)

        then:
        1 * mockLogger.logWarning(_)
    }

    def "saveInboundMessageId should log error and continues when updateMetadataForInboundMessage throws error"() {
        given:
        def inboundMessageId = "inboundId"
        def outboundMessageId = "outboundId"
        mockOrchestrator.updateMetadataForInboundMessage(outboundMessageId, _ as String) >> { throw new PartnerMetadataException("Error") }

        when:
        SendMessageHelper.getInstance().saveInboundMessageId(outboundMessageId, inboundMessageId)

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
        mockOrchestrator.linkMessages(_ as Set<String>) >> {throw new MessageLinkException("", new Exception())}

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
