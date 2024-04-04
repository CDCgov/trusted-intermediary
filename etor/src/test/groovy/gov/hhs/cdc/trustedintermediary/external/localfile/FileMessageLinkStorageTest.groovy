package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

class FileMessageLinkStorageTest extends Specification {

    def messageLinkStorage = FileMessageLinkStorage.getInstance()
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(FileMessageLinkStorage, messageLinkStorage)
        TestApplicationContext.register(Logger, mockLogger)
    }

    def "save and read message links successfully"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def messageId1 = "messageId1"
        var expectedMessageLink1 = new MessageLink(1, Set.of(messageId1, "additionalMessageId1"))
        messageLinkStorage.saveMessageLink(expectedMessageLink1)
        def actualMessageLink1 = messageLinkStorage.getMessageLink(messageId1)

        then:
        actualMessageLink1.isPresent()
        with(actualMessageLink1.get()) {
            linkId == expectedMessageLink1.linkId
            messageIds.containsAll(expectedMessageLink1.messageIds) && expectedMessageLink1.messageIds.containsAll(messageIds)
        }
        0 * mockLogger.logWarning(_ as String)

        when:
        def messageId2 = "messageId2"
        var expectedMessageLink2 = new MessageLink(2, Set.of(messageId2, "additionalMessageId2"))
        messageLinkStorage.saveMessageLink(expectedMessageLink2)
        def actualMessageLink2 = messageLinkStorage.getMessageLink(messageId2)

        then:
        actualMessageLink2.isPresent()
        with(actualMessageLink2.get()) {
            linkId == expectedMessageLink2.linkId
            messageIds.containsAll(expectedMessageLink2.messageIds) && expectedMessageLink2.messageIds.containsAll(messageIds)
        }
        0 * mockLogger.logWarning(_ as String)
    }

    def "getMessageLink throws MessageLinkException when unable to parse file"() {
        given:
        def mockFormatter = Mock(Formatter)
        mockFormatter.convertToJsonString(_) >> "[]"
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> null >> {throw new FormatterProcessingException("error", new Exception())}
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        def submissionId = "submissionId"
        def messageLink = new MessageLink(1, Set.of(submissionId, "messageId2"))
        messageLinkStorage.saveMessageLink(messageLink)

        when:
        messageLinkStorage.getMessageLink(submissionId)

        then:
        thrown(MessageLinkException)
    }

    def "saveMessageLink throws MessageLinkException when unable to save file"() {
        given:
        def messageLink = new MessageLink(1, Set.of("messageId1", "messageId2"))

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertToJsonString(Set.of(messageLink)) >> {throw new FormatterProcessingException("error", new Exception())}
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        messageLinkStorage.saveMessageLink(messageLink)

        then:
        thrown(MessageLinkException)
    }
}
