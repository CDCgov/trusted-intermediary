package gov.hhs.cdc.trustedintermediary.external.database

import com.fasterxml.jackson.databind.ObjectMapper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant

class PostgresDaoTest extends Specification {
    private ConnectionPool mockConnPool
    private Connection mockConn
    private PreparedStatement mockPreparedStatement
    private ResultSet mockResultSet
    private Formatter mockFormatter
    private ObjectMapper testMapper

    private sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
    private sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
    private receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
    private receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockConnPool = Mock(ConnectionPool)
        mockConn = Mock(Connection)
        mockPreparedStatement = Mock(PreparedStatement)
        mockResultSet = Mock(ResultSet)
        mockFormatter = Mock(Formatter)

        testMapper = new ObjectMapper()
        mockResultSet.getString("sending_application_details") >> testMapper.writeValueAsString(sendingApp)
        mockResultSet.getString("sending_facility_details") >> testMapper.writeValueAsString(sendingFacility)
        mockResultSet.getString("receiving_application_details") >> testMapper.writeValueAsString(receivingApp)
        mockResultSet.getString("receiving_facility_details") >> testMapper.writeValueAsString(receivingFacility)

        def mockCredentialsProvider = Mock(DatabaseCredentialsProvider)
        mockCredentialsProvider.getPassword() >> "DogCow password"

        TestApplicationContext.register(DatabaseCredentialsProvider, mockCredentialsProvider)
        TestApplicationContext.register(PostgresDao, PostgresDao.getInstance())
        TestApplicationContext.register(Formatter, mockFormatter)
    }

    def "upsertData works"() {
        given:
        def tableName = "DogCow"
        def pkColumnName = "Moof"
        def columns = [
            new DbColumn(pkColumnName, "Clarus", false, Types.VARCHAR),
            new DbColumn("third_column", Timestamp.from(Instant.now()), false, Types.TIMESTAMP_WITH_TIMEZONE),
            new DbColumn("second_column_with_upsert_overwrite", Timestamp.from(Instant.now()), true, Types.TIMESTAMP_WITH_TIMEZONE),
            new DbColumn("fourth_column_null", null, false, Types.VARCHAR),
            new DbColumn("sending_application_details", sendingApp, false, Types.OTHER),
            new DbColumn("sending_facility_details", sendingFacility, false, Types.OTHER),
            new DbColumn("receiving_application_details", receivingApp, false, Types.OTHER),
            new DbColumn("receiving_facility_details", receivingFacility, false, Types.OTHER),
        ]
        def conflictColumnName = pkColumnName

        mockConnPool.getConnection() >>  mockConn

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertData(tableName, columns, conflictColumnName)

        then:
        mockConn.prepareStatement(_ as String) >> { String sqlStatement ->
            assert sqlStatement.contains(tableName)
            assert sqlStatement.count("?") == columns.size()
            assert sqlStatement.contains("ON CONFLICT (" + pkColumnName + ")")

            assert !sqlStatement.contains(", )")

            // assert that the column names in the SQL statement are in the same order as the list of DbColumns argument
            def beginningOfColumnNamesString = tableName + " ("
            def beginningOfColumnNamesIndex = sqlStatement.indexOf(beginningOfColumnNamesString) + beginningOfColumnNamesString.length()
            def endingOfColumnNamesIndex = sqlStatement.indexOf(")")
            def columnNames = sqlStatement.substring(beginningOfColumnNamesIndex, endingOfColumnNamesIndex)
            def lastFoundColumnNameIndex = -1
            for (int lcv = 0; lcv < columns.size(); lcv++) {
                def columnNameIndex = columnNames.indexOf(columns.get(lcv).name())
                assert columnNameIndex > lastFoundColumnNameIndex
                lastFoundColumnNameIndex = columnNameIndex
            }

            columns.forEach {
                if (!it.upsertOverwrite()) {
                    return
                }
                assert sqlStatement.contains(it.name() + " = EXCLUDED." + it.name())
            }

            return mockPreparedStatement
        }
        (columns.size() - 1)  * mockPreparedStatement.setObject(_ as Integer, _, _ as Integer)
        1 * mockPreparedStatement.setNull(4, Types.VARCHAR)
        1 * mockPreparedStatement.executeUpdate()
    }

    def "upsertData doesn't do any upserts if there is no upsertOverwrite"() {
        given:
        def tableName = "DogCow"
        def columns = [
            new DbColumn("Moof", "Clarus", false, Types.VARCHAR),
            new DbColumn("second_column_with_upsert_overwrite", Timestamp.from(Instant.now()), false, Types.TIMESTAMP_WITH_TIMEZONE),
            new DbColumn("sending_application_details", sendingApp, false, Types.OTHER),
            new DbColumn("sending_facility_details", sendingFacility, false, Types.OTHER),
            new DbColumn("receiving_application_details", receivingApp, false, Types.OTHER),
            new DbColumn("receiving_facility_details", receivingFacility, false, Types.OTHER),
        ]

        mockConnPool.getConnection() >>  mockConn

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertData(tableName, columns, null)

        then:
        mockConn.prepareStatement(_ as String) >> { String sqlStatement ->
            assert sqlStatement.contains(tableName)
            assert sqlStatement.count("?") == columns.size()
            assert !sqlStatement.contains("ON CONFLICT")
            assert !sqlStatement.contains("EXCLUDED")

            return mockPreparedStatement
        }
        6  * mockPreparedStatement.setObject(_ as Integer, _, _ as Integer)
        1 * mockPreparedStatement.executeUpdate()
    }

    def "upsertData unhappy path throws exception"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException() }

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertData("DogCow", [
            new DbColumn("", "", false, Types.VARCHAR),
            new DbColumn("sending_application_details", sendingApp, false, Types.OTHER),
            new DbColumn("sending_facility_details", sendingFacility, false, Types.OTHER),
            new DbColumn("receiving_application_details", receivingApp, false, Types.OTHER),
            new DbColumn("receiving_facility_details", receivingFacility, false, Types.OTHER),
        ], null)

        then:
        thrown(SQLException)
    }

    def "select metadata retrieves data"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true
        mockResultSet.getTimestamp(_ as String) >> Timestamp.from(Instant.now())
        mockResultSet.getString("delivery_status") >> "DELIVERED"
        mockResultSet.getString("message_type") >> "RESULT"
        mockResultSet.getString("placer_order_number") >> "placer_order_number"
        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
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
        def placerOrderNumber = "placer_order_number"
        def expected = new PartnerMetadata(receivedMessageId, sentMessageId, sender, receiver, timeReceived, timeDelivered, hash, status, reason, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

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
        mockResultSet.getString("placer_order_number") >> placerOrderNumber
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
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
        mockResultSet.getString("placer_order_number") >> "TEST"
        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
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
        def expected1 = new PartnerMetadata("12345", "7890", sender, "You'll get your just reward",
                Instant.parse("2024-01-03T15:45:33.30Z"), Instant.parse("2024-01-03T15:45:33.30Z"),  sender.hashCode().toString(),
                PartnerMetadataStatus.PENDING, "It done Goofed", messageType, sendingApp, sendingFacility,
                receivingApp, receivingFacility, "placer_order_number")
        def expected2 = new PartnerMetadata("doreyme", "fasole", sender, "receiver",
                Instant.now(), Instant.now(), "gobeltygoook",
                PartnerMetadataStatus.DELIVERED, "cause I said so", messageType, sendingApp, sendingFacility,
                receivingApp, receivingFacility, "placer_order_number")

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
        mockResultSet.getString("sending_application_details") >>> [
            testMapper.writeValueAsString(sendingApp),
            testMapper.writeValueAsString(sendingApp)
        ]
        mockResultSet.getString("sending_facility_details") >>> [
            testMapper.writeValueAsString(sendingFacility),
            testMapper.writeValueAsString(sendingFacility),
        ]
        mockResultSet.getString("receiving_application_details") >>> [
            testMapper.writeValueAsString(receivingApp),
            testMapper.writeValueAsString(receivingApp)
        ]
        mockResultSet.getString("receiving_facility_details") >>> [
            testMapper.writeValueAsString(receivingFacility),
            testMapper.writeValueAsString(receivingFacility)
        ]
        mockResultSet.getString("placer_order_number") >>> [
            expected1.placerOrderNumber(),
            expected2.placerOrderNumber()
        ]
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadataForSender("sender")

        then:
        actual.containsAll(Set.of(expected1, expected2))
    }

    def "fetchMetadata throws exception for FormatterProcessingException"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true
        mockResultSet.getTimestamp("time_received") >> null
        mockResultSet.getString("delivery_status") >> "DELIVERED"
        mockResultSet.getString("message_type") >> "RESULT"
        mockResultSet.getString("failure_reason") >> "Your time is up"
        mockResultSet.getString("placer_order_number") >> "TEST"
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> { throw new FormatterProcessingException('error', new Throwable()) }

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        thrown(FormatterProcessingException)
    }
}
