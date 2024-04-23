package gov.hhs.cdc.trustedintermediary.etor.messagelink

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class MessageLinkTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(MessageLink.class)

        then:
        noExceptionThrown()
    }

    def "test equals and hashCode"() {
        when:
        PojoTestUtils.validateEqualsAndHashCode(MessageLink.class)

        then:
        noExceptionThrown()
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
}
