package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import spock.lang.Specification

class DatabaseMessageLinkStorageTest extends Specification {

    private ConnectionPool mockConnPool
    private DbDao mockDao

    def mockMessageLinkData = new MessageLink(UUID.randomUUID(), "TestMessageId")

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockDao = Mock(DbDao)
        mockConnPool = Mock(ConnectionPool)

        TestApplicationContext.register(DbDao, mockDao)
        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.register(DatabaseMessageLinkStorage, DatabaseMessageLinkStorage.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }


    def "getMessageLink returns message link when rows exist"() {
        given:
        def linkId = UUID.randomUUID()
        def messageId = "MessageId"
        def messageLink = new MessageLink(linkId, messageId)
        def expected = Optional.of(messageLink)

        def mockConn = Mock(Connection)
        def mockPreparedStatement = Mock(PreparedStatement)
        def mockResultSet = Mock(ResultSet)

        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        // First run returns true, then return false
        mockResultSet.next() >> true >> false
        mockResultSet.getString("link_id") >> linkId
        mockResultSet.getString("message_id") >> messageId

        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DatabaseMessageLinkStorage.getInstance().getMessageLink(messageId)

        then:
        actual.get().getLinkId() == expected.get().getLinkId()
    }


    def "getMessageLink returns empty optional when rows do not exist"() {
        given:
        def mockConn = Mock(Connection)
        def mockPreparedStatement = Mock(PreparedStatement)
        def mockResultSet = Mock(ResultSet)

        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >> false
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DatabaseMessageLinkStorage.getInstance().getMessageLink("mock_lookup")

        then:
        actual == Optional.empty()
    }

    def "getMessageLink throws SQLException if something goes wrong"() {
        given:
        def mockConn
        def mockPreparedStatement
        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        mockConn = Mock(Connection)
        mockPreparedStatement = Mock(PreparedStatement)
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockPreparedStatement.executeQuery() >> { throw new SQLException("Something went wrong!") }
        DatabaseMessageLinkStorage.getInstance().getMessageLink("TestSubmissionId")

        then:
        thrown(Exception)

        when:
        mockConn = Mock(Connection)
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException("Something went wrong!") }
        DatabaseMessageLinkStorage.getInstance().getMessageLink("TestSubmissionId")

        then:
        thrown(Exception)

        when:
        mockConnPool.getConnection() >> { throw new SQLException("Something went wrong!") }
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
