package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException
import java.sql.SQLException
import spock.lang.Specification

class DatabaseMessageLinkStorageTest extends Specification {

    private def mockDao

    def mockMessageLinkData = new MessageLink(UUID.randomUUID(), "TestMessageId")

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockDao = Mock(DbDao)

        TestApplicationContext.register(DbDao, mockDao)
        TestApplicationContext.register(DatabaseMessageLinkStorage, DatabaseMessageLinkStorage.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "getMessageLink happy path works"() {
        given:
        def expectedResult = Optional.of(mockMessageLinkData)

        mockDao.fetchMessageLink(_ as String) >> Optional.of(mockMessageLinkData)

        when:
        def actualResult = DatabaseMessageLinkStorage.getInstance().getMessageLink("TestSubmissionId")

        then:
        actualResult == expectedResult
    }

    def "getMessageLink unhappy path works"() {
        given:
        mockDao.fetchMessageLink(_ as String) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabaseMessageLinkStorage.getInstance().getMessageLink("TestSubmissionId")

        then:
        thrown(Exception)
    }

    def "saveLinkedMessages happy path works"() {
        given:
        def messageIdCount = mockMessageLinkData.getMessageIds().size()

        when:
        DatabaseMessageLinkStorage.getInstance().saveMessageLink(mockMessageLinkData)

        then:
        messageIdCount * mockDao.upsertData("message_link", _ as List<DbColumn>, "message_id")
    }

    def "saveMessageLink unhappy path works"() {
        given:
        mockDao.upsertData("message_link", _ as List<DbColumn>, "message_id") >> { throw new SQLException("Something went wrong!") }

        when:
        DatabaseMessageLinkStorage.getInstance().saveMessageLink(mockMessageLinkData)

        then:
        thrown(MessageLinkException)
    }
}
