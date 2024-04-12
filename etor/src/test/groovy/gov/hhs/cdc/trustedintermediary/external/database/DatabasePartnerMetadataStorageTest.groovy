package gov.hhs.cdc.trustedintermediary.external.database

import com.fasterxml.jackson.databind.ObjectMapper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.util.function.Function
import spock.lang.Specification

class DatabasePartnerMetadataStorageTest extends Specification {

    private def mockDao
    private def mockFormatter

    def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
    def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
    def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
    def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
    def mockMetadata = new PartnerMetadata("receivedSubmissionId", "sentSubmissionId","sender", "receiver", Instant.now(), Instant.now(), "hash", PartnerMetadataStatus.DELIVERED, "failure reason", PartnerMetadataMessageType.ORDER, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockDao = Mock(DbDao)
        mockFormatter = Mock(Formatter)

        TestApplicationContext.register(DbDao, mockDao)
        TestApplicationContext.register(PartnerMetadataStorage, DatabasePartnerMetadataStorage.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "readMetadata happy path works"() {
        given:
        def expectedResult = Optional.of(mockMetadata)

        mockDao.fetchFirstData(_ as Function<Connection, PreparedStatement>, _ as Function<ResultSet, PartnerMetadata>) >> mockMetadata

        when:
        def actualResult = DatabasePartnerMetadataStorage.getInstance().readMetadata(mockMetadata.receivedSubmissionId())

        then:
        actualResult == expectedResult
    }

    def "readMetadata unhappy path works"() {
        given:
        mockDao.fetchFirstData(_ as Function<Connection, PreparedStatement>, _ as Function<ResultSet, PartnerMetadata>) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabasePartnerMetadataStorage.getInstance().readMetadata("receivedSubmissionId")

        then:
        thrown(PartnerMetadataException)
    }

    def "readMetadataForSender unhappy path triggers SQLException"() {
        given:
        mockDao.fetchManyData(_ as Function<Connection, PreparedStatement>, _ as Function<ResultSet, PartnerMetadata>, _) >> { throw new SQLException("Database error has occur") }

        when:
        DatabasePartnerMetadataStorage.getInstance().readMetadataForSender("testSender")

        then:
        thrown(PartnerMetadataException)
    }

    def "readMetadataForSender happy path works"() {
        given:
        def metadata1 = mockMetadata
        def metadata2 = mockMetadata
        def expectedMetadataSet = new HashSet<>()
        expectedMetadataSet.add(metadata1)
        expectedMetadataSet.add(metadata2)

        mockDao.fetchManyData(_ as Function<Connection, PreparedStatement>, _ as Function<ResultSet, PartnerMetadata>, _) >> expectedMetadataSet

        when:
        def actualMetadataSet = DatabasePartnerMetadataStorage.getInstance().readMetadataForSender("testSender")

        then:
        actualMetadataSet.size() == expectedMetadataSet.size()
        actualMetadataSet.containsAll(expectedMetadataSet)
    }

    def "saveMetadata happy path works"() {
        given:
        def testMapper = new ObjectMapper()
        List<DbColumn> columns =
                List.of(
                new DbColumn("received_message_id", mockMetadata.receivedSubmissionId(), false, Types.VARCHAR),
                new DbColumn("sent_message_id", mockMetadata.sentSubmissionId(), true, Types.VARCHAR),
                new DbColumn("sender", mockMetadata.sender(), false, Types.VARCHAR),
                new DbColumn("receiver", mockMetadata.receiver(), true, Types.VARCHAR),
                new DbColumn("hash_of_message", mockMetadata.hash(), false, Types.VARCHAR),
                new DbColumn("time_received", Timestamp.from(mockMetadata.timeReceived()),false, Types.TIMESTAMP),
                new DbColumn("time_delivered", Timestamp.from(mockMetadata.timeDelivered()),true, Types.TIMESTAMP),
                new DbColumn("delivery_status", mockMetadata.deliveryStatus().toString(),true,Types.OTHER),
                new DbColumn("failure_reason", mockMetadata.failureReason(), true, Types.VARCHAR),
                new DbColumn("message_type", mockMetadata.messageType().toString(), false, Types.OTHER),
                new DbColumn("placer_order_number", mockMetadata.placerOrderNumber(), false, Types.VARCHAR),
                new DbColumn("sending_application_details", testMapper.writeValueAsString(mockMetadata.sendingApplicationDetails()), false, Types.OTHER),
                new DbColumn("sending_facility_details", testMapper.writeValueAsString(mockMetadata.sendingFacilityDetails()), false, Types.OTHER),
                new DbColumn("receiving_application_details", testMapper.writeValueAsString(mockMetadata.receivingApplicationDetails()), false, Types.OTHER),
                new DbColumn("receiving_facility_details", testMapper.writeValueAsString(mockMetadata.receivingFacilityDetails()), false, Types.OTHER)
                )
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        1 * mockDao.upsertData("metadata", columns, "received_message_id")
    }

    def "saveMetadata unhappy path works"() {
        given:
        mockDao.upsertData(_ as String, _ as List, _ as String) >> { throw new SQLException("Something went wrong!") }
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata unhappy path first format call triggers FormatterProcessingException"() {
        given:
        mockFormatter.convertToJsonString(_ as MessageHdDataType) >> { throw new FormatterProcessingException('error', new Throwable()) }

        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata second unhappy path format call triggers FormatterProcessingException"() {
        given:
        mockFormatter.convertToJsonString(_ as MessageHdDataType) >> "ok" >> { throw new FormatterProcessingException('error', new Throwable()) }

        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata third unhappy path format call triggers FormatterProcessingException"() {
        given:
        mockFormatter.convertToJsonString(_ as MessageHdDataType) >> ["ok", "ok"] >> { throw new FormatterProcessingException('error', new Throwable()) }

        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata fourth unhappy path format call triggers FormatterProcessingException"() {
        given:
        mockFormatter.convertToJsonString(_ as MessageHdDataType) >> ["ok", "ok" , "ok"] >> { throw new FormatterProcessingException('error', new Throwable()) }

        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata writes null timestamp"() {
        given:
        def testMapper = new ObjectMapper()
        def mockMetadata = new PartnerMetadata(
                "receivedSubmissionId",
                "sentSubmissionId",
                "sender",
                "receiver",
                null,
                null,
                "hash",
                null, // PartnerMetadata defaults deliveryStatus to PENDING on null, so that's why we're asserting not-null bellow
                "DogCow failure",
                null,
                sendingApp,
                sendingFacility,
                receivingApp,
                receivingFacility,
                "placer_order_number"
                )

        List<DbColumn> columns =
                List.of(
                new DbColumn("received_message_id", mockMetadata.receivedSubmissionId(), false, Types.VARCHAR),
                new DbColumn("sent_message_id", mockMetadata.sentSubmissionId(), true, Types.VARCHAR),
                new DbColumn("sender", mockMetadata.sender(), false, Types.VARCHAR),
                new DbColumn("receiver", mockMetadata.receiver(), true, Types.VARCHAR),
                new DbColumn("hash_of_message", mockMetadata.hash(), false, Types.VARCHAR),
                new DbColumn("time_received", null, false, Types.TIMESTAMP),
                new DbColumn("time_delivered", null,true, Types.TIMESTAMP),
                new DbColumn("delivery_status", mockMetadata.deliveryStatus().toString(), true,Types.OTHER),
                new DbColumn("failure_reason", mockMetadata.failureReason(), true, Types.VARCHAR),
                new DbColumn("message_type", null, false, Types.OTHER),
                new DbColumn("placer_order_number", mockMetadata.placerOrderNumber(), false, Types.VARCHAR),
                new DbColumn("sending_application_details", testMapper.writeValueAsString(mockMetadata.sendingApplicationDetails()), false, Types.OTHER),
                new DbColumn("sending_facility_details", testMapper.writeValueAsString(mockMetadata.sendingFacilityDetails()), false, Types.OTHER),
                new DbColumn("receiving_application_details", testMapper.writeValueAsString(mockMetadata.receivingApplicationDetails()), false, Types.OTHER),
                new DbColumn("receiving_facility_details", testMapper.writeValueAsString(mockMetadata.receivingFacilityDetails()), false, Types.OTHER)
                )
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        1 * mockDao.upsertData("metadata", columns, "received_message_id")
    }

    def "partnerMetadataFromResultSet throws exception due to FormatterProcessingException"() {
        given:
        def mockResultSet = Mock(ResultSet)
        mockResultSet.next() >> true
        mockResultSet.getString("delivery_status") >> "DELIVERED"
        mockResultSet.getString("message_type") >> "RESULT"
        mockResultSet.getString("sending_application_details") >> "TEST"

        def innerThrownException = new FormatterProcessingException('error', new Throwable())

        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> { throw innerThrownException }
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        DatabasePartnerMetadataStorage.getInstance().partnerMetadataFromResultSet(mockResultSet)

        then:
        def thrownException = thrown(RuntimeException)
        thrownException.getCause() == innerThrownException
    }

    def "partnerMetadataFromResultSet returns partner metadata"() {
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

        def mockResultSet = Mock(ResultSet)
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
        mockResultSet.getString("receiving_facility_details") >> "receiving_facility_details"
        mockResultSet.getString("sending_application_details") >> "sending_application_details"
        mockResultSet.getString("sending_facility_details") >> "sending_facility_details"
        mockResultSet.getString("receiving_application_details") >> "receiving_application_details"
        mockResultSet.getString("placer_order_number") >> placerOrderNumber

        mockFormatter.convertJsonToObject("receiving_facility_details", _ as TypeReference) >> receivingFacility
        mockFormatter.convertJsonToObject("sending_application_details", _ as TypeReference) >> sendingApp
        mockFormatter.convertJsonToObject("sending_facility_details", _ as TypeReference) >> sendingFacility
        mockFormatter.convertJsonToObject("receiving_application_details", _ as TypeReference) >> receivingApp

        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DatabasePartnerMetadataStorage.getInstance().partnerMetadataFromResultSet(mockResultSet)

        then:
        actual == expected
    }

    def "partnerMetadataFromResultSet successfully sets the received and delivered timestamp to null"() {
        given:
        def mockResultSet = Mock(ResultSet)
        mockResultSet.next() >> true
        mockResultSet.getTimestamp("time_received") >> null
        mockResultSet.getTimestamp("time_delivered") >> null
        mockResultSet.getString("delivery_status") >> "DELIVERED"
        mockResultSet.getString("message_type") >> "RESULT"

        TestApplicationContext.register(Formatter, Mock(Formatter))
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DatabasePartnerMetadataStorage.getInstance().partnerMetadataFromResultSet(mockResultSet)

        then:
        actual.timeReceived() == null
    }
}
