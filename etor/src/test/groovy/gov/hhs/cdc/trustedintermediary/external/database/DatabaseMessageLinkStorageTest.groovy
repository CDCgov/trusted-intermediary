package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.function.Function
import spock.lang.Specification

class DatabaseMessageLinkStorageTest extends Specification {

    private DbDao mockDao

    def mockMessageLinkData = new MessageLink(UUID.randomUUID(), "TestMessageId")

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockDao = Mock(DbDao)

        TestApplicationContext.register(DbDao, mockDao)
        TestApplicationContext.register(DatabaseMessageLinkStorage, DatabaseMessageLinkStorage.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "getMessageLink returns message link"() {
        given:
        def linkId = UUID.randomUUID()
        def getMessageId = "getMessageId"
        def additionalMessageId = "additionalMessageId"
        def messageLink = new MessageLink(linkId, Set.of(getMessageId, additionalMessageId))

        mockDao.fetchManyData(_ as Function<Connection, PreparedStatement>, _ as Function<ResultSet, Map<UUID, String>>, _) >> [
            [(linkId): getMessageId],
            [(linkId): additionalMessageId]
        ].toSet()

        when:
        def actual = DatabaseMessageLinkStorage.getInstance().getMessageLink(getMessageId)

        then:
        actual.get() == messageLink
    }

    def "getMessageLink returns empty optional when not exist"() {
        given:
        mockDao.fetchManyData(_ as Function<Connection, PreparedStatement>, _ as Function<ResultSet, Map<UUID, String>>, _) >> [].toSet()

        when:
        def actual = DatabaseMessageLinkStorage.getInstance().getMessageLink("mock_lookup")

        then:
        actual == Optional.empty()
    }

    def "getMessageLink throws MessageLinkException if something goes wrong"() {
        given:
        mockDao.fetchManyData(_ as Function<Connection, PreparedStatement>, _ as Function<ResultSet, Map<UUID, String>>, _) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabaseMessageLinkStorage.getInstance().getMessageLink("TestSubmissionId")

        then:
        thrown(MessageLinkException)
    }

    def "partialMessageLinkFromResultSet throws exception if something goes wrong"() {
        given:
        def resultSet = Mock(ResultSet)
        def originalException = new SQLException("DB goes boom")
        resultSet.getString(_ as String) >> { throw originalException }

        when:
        DatabaseMessageLinkStorage.getInstance().partialMessageLinkFromResultSet(resultSet)

        then:
        def thrownException = thrown(RuntimeException)
        thrownException.getCause() == originalException
    }

    def "saveLinkedMessages happy path works"() {
        given:
        def messageIdCount = mockMessageLinkData.getMessageIds().size()

        when:
        DatabaseMessageLinkStorage.getInstance().saveMessageLink(mockMessageLinkData)

        then:
        messageIdCount * mockDao.upsertData("message_link", _ as List<DbColumn>, _ as String)
    }

    def "saveMessageLink unhappy path works"() {
        given:
        mockDao.upsertData("message_link", _ as List<DbColumn>, _ as String) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabaseMessageLinkStorage.getInstance().saveMessageLink(mockMessageLinkData)

        then:
        thrown(MessageLinkException)
    }
}
