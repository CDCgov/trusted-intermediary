package gov.hhs.cdc.trustedintermediary.etor.messagelink

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

class MessageLinkTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
    }

    def "constructor without parameters initializes with empty messageIds"() {
        when:
        def messageLink = new MessageLink()

        then:
        messageLink.getMessageIds().isEmpty()
    }

    def "constructor with linkId and single messageId initializes correctly"() {
        given:
        def linkId = UUID.randomUUID()
        def messageId = "messageId"

        when:
        def messageLink = new MessageLink(linkId, messageId)

        then:
        messageLink.getLinkId() == linkId
        messageLink.getMessageIds().size() == 1
        messageLink.getMessageIds().contains(messageId)
    }

    def "constructor with linkId and messageId set initializes correctly"() {
        given:
        def linkId = UUID.randomUUID()
        def messageIdSet = Set.of("messageId1", "messageId2")

        when:
        def messageLink = new MessageLink(linkId, messageIdSet)

        then:
        messageLink.getLinkId() == linkId
        messageLink.getMessageIds() == messageIdSet
    }

    def "addMessageId adds messageId to the messageIds set"() {
        given:
        def messageLink = new MessageLink()
        String messageId = "messageId"

        when:
        messageLink.addMessageId(messageId)

        then:
        messageLink.getMessageIds().size() == 1
        messageLink.getMessageIds().contains(messageId)
    }

    def "addMessageIds adds all messageIds to the messageIds set"() {
        given:
        def messageLink = new MessageLink()
        def newMessageIds = Set.of("messageId1", "messageId2")

        when:
        messageLink.addMessageIds(newMessageIds)

        then:
        messageLink.getMessageIds().size() == 2
        messageLink.getMessageIds().containsAll(newMessageIds)
    }

    def "adding message ids doesn't duplicate existing ids"() {
        given:
        def existingMessageId = "messageId1"
        def messageIds = Set.of(existingMessageId, "messageId2")
        def messageLink = new MessageLink(UUID.randomUUID(), messageIds)

        when:
        messageLink.addMessageId(existingMessageId)

        then:
        messageLink.getMessageIds().size() == 2
        messageLink.getMessageIds() == messageIds

        when:
        messageLink.addMessageIds(messageIds)

        then:
        messageLink.getMessageIds() == messageIds
    }

    def "setLinkId sets the linkId"() {
        given:
        def messageLink = new MessageLink()
        def linkId = UUID.randomUUID()

        when:
        messageLink.setLinkId(linkId)

        then:
        messageLink.getLinkId() == linkId
    }

    def "setMessageIds replaces the entire messageIds set"() {
        given:
        def messageLink = new MessageLink(UUID.randomUUID(), "messageId")
        def newMessageIds = Set.of("messageId1", "messageId2")

        when:
        messageLink.setMessageIds(newMessageIds)

        then:
        messageLink.getMessageIds() == newMessageIds
    }
}
