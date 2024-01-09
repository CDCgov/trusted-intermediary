package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.external.azure.AzureClient
import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager
import spock.lang.Specification

import java.sql.Timestamp
import java.time.Instant

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class PostgresDaoTest extends Specification {

    private def mockDriver
    private def mockConn
    private def mockPreparedStatement
    private def mockResultSet

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockDriver = Mock(SqlDriverManager)
        mockConn = Mock(Connection)
        mockPreparedStatement = Mock(PreparedStatement)
        mockResultSet = Mock(ResultSet)
        def mockAzureClient = Mock(AzureClient)
        mockAzureClient.getScopedToken(_ as String) >> "DogCow password"

        TestApplicationContext.register(AzureClient, mockAzureClient)
        TestApplicationContext.register(PostgresDao, PostgresDao.getInstance())
    }

    def "connect happy path works"(){
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> {mockConn}

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def conn = PostgresDao.getInstance().connect()

        then:
        conn == mockConn
    }

    def "connect unhappy path throws exception"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> {throw new SQLException()}
        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().connect()

        then:
        thrown(SQLException)
    }

    def "upsertMetadata works"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >>  mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata("mock_id_receiver", "mock_id_sender", "mock_sender", "mock_receiver", "mock_hash", Instant.now())

        then:
        1 * mockPreparedStatement.executeUpdate()
    }

    def "upsertMetadata unhappy path throws exception"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException() }

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata("mock_id_receiver", "mock_id_sender", "mock_sender", "mock_receiver", "mock_hash", Instant.now())

        then:
        thrown(SQLException)
    }

    def "upsertMetadata writes null timestamp"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >>  mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata("mock_id_receiver", "mock_id_sender", "mock_sender", "mock_receiver", "mock_hash", null)

        then:
        mockPreparedStatement.setTimestamp(_ as Integer, _) >> { Integer parameterIndex, Timestamp timestamp ->
            assert timestamp == null
        }
    }

    def "select metadata retrieves data"(){
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true
        mockResultSet.getTimestamp(_ as String) >> Timestamp.from(Instant.now())

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadata result = (PartnerMetadata) PostgresDao.getInstance().fetchMetadata("mock_sender")

        then:
        result != null
    }

    def "fetchMetadata unhappy path throws exception"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException() }

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        thrown(SQLException)
    }

    def "fetchMetadata returns null when rows do not exist"() {
        given:
        def expected = null

        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >> false
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        actual == expected
    }

    def "fetchMetadata returns partnermetadata when rows exist"() {
        given:
        def receivedMessageId = "12345"
        def sentMessageId = "7890"
        def sender = "DogCow"
        Timestamp timestampForMock = Timestamp.from(Instant.parse("2024-01-03T15:45:33.30Z"))
        Instant timeReceived = timestampForMock.toInstant()
        def hash = sender.hashCode().toString()
        def expected = new PartnerMetadata(receivedMessageId, sentMessageId, sender, null, timeReceived, hash)

        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >> true
        mockResultSet.getString("received_message_id") >> receivedMessageId
        mockResultSet.getString("sent_message_id") >> sentMessageId
        mockResultSet.getString("sender") >> sender
        mockResultSet.getString("receiver") >> null
        mockResultSet.getTimestamp("time_received") >> timestampForMock
        mockResultSet.getString("hash_of_order") >> hash
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        actual == expected
    }

    def "fetchMetadata successfully sets the received timestamp to null"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true
        mockResultSet.getTimestamp("time_received") >> null

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        actual.timeReceived() == null
    }
}
