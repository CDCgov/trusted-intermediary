package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import spock.lang.Specification

class PostgresDaoTest extends Specification {

    private ConnectionPool mockConnPool
    private Connection mockConn
    private PreparedStatement mockPreparedStatement
    private ResultSet mockResultSet

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockConnPool = Mock(ConnectionPool)
        mockConn = Mock(Connection)
        mockPreparedStatement = Mock(PreparedStatement)
        mockResultSet = Mock(ResultSet)
        def mockCredentialsProvider = Mock(DatabaseCredentialsProvider)
        mockCredentialsProvider.getPassword() >> "DogCow password"

        TestApplicationContext.register(DatabaseCredentialsProvider, mockCredentialsProvider)
        TestApplicationContext.register(PostgresDao, PostgresDao.getInstance())
    }


    def "upsertMetadata works"() {
        given:
        def receivedSubmissionId = "mock_id_receiver"
        def sentSubmissionId = "mock_id_sender"
        def sender = "mock_sender"
        def receiver = "mock_receiver"
        def hash = "mock_hash"
        def timestamp = Instant.now()
        def status = PartnerMetadataStatus.PENDING
        def failureReason = "failure reason"
        def messageType = PartnerMetadataMessageType.RESULT

        mockConnPool.getConnection() >>  mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, hash, timestamp, timestamp, status, failureReason, messageType)

        then:
        1 * mockPreparedStatement.setString(1, receivedSubmissionId)
        1 * mockPreparedStatement.setString(2, sentSubmissionId)
        1 * mockPreparedStatement.setString(3, sender)
        1 * mockPreparedStatement.setString(4, receiver)
        1 * mockPreparedStatement.setString(5, hash)
        1 * mockPreparedStatement.setTimestamp(6, Timestamp.from(timestamp))
        1 * mockPreparedStatement.setTimestamp(7, Timestamp.from(timestamp))
        1 * mockPreparedStatement.setObject(8, status.toString(), Types.OTHER)
        1 * mockPreparedStatement.setString(9, failureReason)
        1 * mockPreparedStatement.executeUpdate()
    }

    def "upsertMetadata unhappy path throws exception"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException() }

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata("mock_id_receiver", "mock_id_sender", "mock_sender", "mock_receiver", "mock_hash", Instant.now(), Instant.now(), PartnerMetadataStatus.DELIVERED, "mock_failure_reason", PartnerMetadataMessageType.RESULT)

        then:
        thrown(SQLException)
    }

    def "upsertMetadata writes null timestamp"() {
        given:
        mockConnPool.getConnection() >>  mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata("mock_id_receiver", "mock_id_sender", "mock_sender", "mock_receiver", "mock_hash", null, null, PartnerMetadataStatus.DELIVERED, "mock_failure_reason", PartnerMetadataMessageType.RESULT)

        then:
        mockPreparedStatement.setTimestamp(_ as Integer, _ as Timestamp) >> { Integer parameterIndex, Timestamp timestamp ->
            assert timestamp == null
        }
    }

    def "select metadata retrieves data"(){
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true
        mockResultSet.getTimestamp(_ as String) >> Timestamp.from(Instant.now())
        mockResultSet.getString("delivery_status") >> "DELIVERED"
        mockResultSet.getString("message_type") >> "RESULT"
        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadata result = (PartnerMetadata) PostgresDao.getInstance().fetchMetadata("mock_sender")

        then:
        result != null
    }

    def "fetchMetadata unhappy path throws exception"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException() }

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        thrown(SQLException)
    }

    def "fetchMetadata returns null when rows do not exist"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >> false
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        actual == null
    }

    def "fetchMetadata returns partner metadata when rows exist"() {
        given:
        def receivedMessageId = "12345"
        def sentMessageId = "7890"
        def sender = "DogCow"
        def receiver = "You'll get your just reward"
        Timestamp timestampForMock = Timestamp.from(Instant.parse("2024-01-03T15:45:33.30Z"))
        Instant timeReceived = timestampForMock.toInstant()
        Timestamp mockDeliveredTimestamp = Timestamp.from(Instant.parse("2024-01-31T11:07:53.00Z"))
        Instant timeDelivered = mockDeliveredTimestamp.toInstant()
        def hash = sender.hashCode().toString()
        def status = PartnerMetadataStatus.PENDING
        def reason = "It done Goofed"
        def messageType = PartnerMetadataMessageType.RESULT
        def expected = new PartnerMetadata(receivedMessageId, sentMessageId, sender, receiver, timeReceived, timeDelivered, hash, status, reason, messageType)

        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >> true
        mockResultSet.getString("received_message_id") >> receivedMessageId
        mockResultSet.getString("sent_message_id") >> sentMessageId
        mockResultSet.getString("sender") >> sender
        mockResultSet.getString("receiver") >> receiver
        mockResultSet.getTimestamp("time_received") >> timestampForMock
        mockResultSet.getTimestamp("time_delivered") >> mockDeliveredTimestamp
        mockResultSet.getString("hash_of_message") >> hash
        mockResultSet.getString("delivery_status") >> status.toString()
        mockResultSet.getString("failure_reason") >> reason
        mockResultSet.getString("message_type") >> messageType.toString()
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        actual == expected
    }

    def "fetchMetadata successfully sets the received timestamp to null"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true
        mockResultSet.getTimestamp("time_received") >> null
        mockResultSet.getString("delivery_status") >> "DELIVERED"
        mockResultSet.getString("message_type") >> "RESULT"
        mockResultSet.getString("failure_reason") >> "Your time is up"
        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        actual.timeReceived() == null
    }

    def "fetchMetadataForSender retrieves a set of PartnerMetadata"() {
        given:
        def sender = "DogCow"
        def messageType = PartnerMetadataMessageType.RESULT
        def expected1 = new PartnerMetadata("12345", "7890", sender, "You'll get your just reward", Instant.parse("2024-01-03T15:45:33.30Z"),Instant.parse("2024-01-03T15:45:33.30Z"),  sender.hashCode().toString(), PartnerMetadataStatus.PENDING, "It done Goofed", messageType)
        def expected2 = new PartnerMetadata("doreyme", "fasole", sender, "receiver", Instant.now(), Instant.now(), "gobeltygoook", PartnerMetadataStatus.DELIVERED, "cause I said so", messageType)

        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >>> [true, true, false]
        mockResultSet.getString("received_message_id") >>> [
            expected1.receivedSubmissionId(),
            expected2.receivedSubmissionId()
        ]
        mockResultSet.getString("sent_message_id") >>> [
            expected1.sentSubmissionId(),
            expected2.sentSubmissionId()
        ]
        mockResultSet.getString("sender") >>> [
            expected1.sender(),
            expected2.sender()
        ]
        mockResultSet.getString("receiver") >>> [
            expected1.receiver(),
            expected2.receiver()
        ]
        mockResultSet.getTimestamp("time_received") >>> [
            Timestamp.from(expected1.timeReceived()),
            Timestamp.from(expected2.timeReceived())
        ]
        mockResultSet.getTimestamp("time_delivered") >>> [
            Timestamp.from(expected1.timeDelivered()),
            Timestamp.from(expected2.timeDelivered())
        ]
        mockResultSet.getString("hash_of_message") >>> [
            expected1.hash(),
            expected2.hash()
        ]
        mockResultSet.getString("delivery_status") >>> [
            expected1.deliveryStatus().toString(),
            expected2.deliveryStatus().toString()
        ]
        mockResultSet.getString("failure_reason") >>> [
            expected1.failureReason(),
            expected2.failureReason()
        ]
        mockResultSet.getString("message_type") >>> [
            expected1.messageType().toString(),
            expected2.messageType().toString()
        ]
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()


        when:
        def actual = PostgresDao.getInstance().fetchMetadataForSender("sender")

        then:
        actual.containsAll(Set.of(expected1, expected2))
    }
}
