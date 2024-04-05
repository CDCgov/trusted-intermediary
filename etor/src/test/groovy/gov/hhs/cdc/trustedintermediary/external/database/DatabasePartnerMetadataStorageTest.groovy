package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import spock.lang.Specification

class DatabasePartnerMetadataStorageTest extends Specification {

    private def mockDao

    def mockMetadata = new PartnerMetadata("receivedSubmissionId", "sentSubmissionId","sender", "receiver", Instant.now(), Instant.now(), "hash", PartnerMetadataStatus.DELIVERED, "failure reason", PartnerMetadataMessageType.ORDER, "sending_app", "sending_facility", "receiving_app", "receiving_facility", "placer_order_number")

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockDao = Mock(DbDao)

        TestApplicationContext.register(DbDao, mockDao)
        TestApplicationContext.register(PartnerMetadataStorage, DatabasePartnerMetadataStorage.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "readMetadata happy path works"() {
        given:
        def expectedResult = Optional.of(mockMetadata)

        mockDao.fetchMetadata(_ as String) >> mockMetadata

        when:
        def actualResult = DatabasePartnerMetadataStorage.getInstance().readMetadata(mockMetadata.receivedSubmissionId())

        then:
        actualResult == expectedResult
    }

    def "readMetadata unhappy path works"() {
        given:
        mockDao.fetchMetadata(_ as String) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabasePartnerMetadataStorage.getInstance().readMetadata("receivedSubmissionId")

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata happy path works"() {
        given:
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
                new DbColumn("sending_application_id", mockMetadata.sendingApplicationId(), false, Types.VARCHAR),
                new DbColumn("sending_facility_id", mockMetadata.sendingFacilityId(), false, Types.VARCHAR),
                new DbColumn("receiving_application_id", mockMetadata.receivingApplicationId(), false, Types.VARCHAR),
                new DbColumn("receiving_facility_id", mockMetadata.receivingFacilityId(), false, Types.VARCHAR)
                )

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        1 * mockDao.upsertData("metadata", columns, "received_message_id")
    }

    def "saveMetadata unhappy path works"() {
        given:
        mockDao.upsertData(_ as String, _ as List, _ as String) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata writes null timestamp"() {
        given:
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
                "sending_app",
                "sending_facility",
                "receiving_app",
                "receiving_facility",
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
                new DbColumn("sending_application_id", mockMetadata.sendingApplicationId(), false, Types.VARCHAR),
                new DbColumn("sending_facility_id", mockMetadata.sendingFacilityId(), false, Types.VARCHAR),
                new DbColumn("receiving_application_id", mockMetadata.receivingApplicationId(), false, Types.VARCHAR),
                new DbColumn("receiving_facility_id", mockMetadata.receivingFacilityId(), false, Types.VARCHAR)
                )

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        1 * mockDao.upsertData("metadata", columns, "received_message_id")
    }

    def "readMetadataForMessageLinking happy path works"() {
        given:
        def expectedResult = Set.of(mockMetadata)

        mockDao.fetchMetadataForMessageLinking(_ as String) >> expectedResult

        when:
        def actualResult = DatabasePartnerMetadataStorage.getInstance().readMetadataForMessageLinking(mockMetadata.receivedSubmissionId())

        then:
        actualResult == expectedResult
    }

    def "readMetadataForMessageLinking unhappy path works"() {
        given:
        mockDao.fetchMetadataForMessageLinking(_ as String) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabasePartnerMetadataStorage.getInstance().readMetadataForMessageLinking("receivedSubmissionId")

        then:
        thrown(PartnerMetadataException)
    }

    def "readMetadataForSender happy path works"() {
        given:
        def expectedResult = Set.of(mockMetadata)

        mockDao.fetchMetadataForSender(_ as String) >> expectedResult

        when:
        def actualResult = DatabasePartnerMetadataStorage.getInstance().readMetadataForSender("TestSender")

        then:
        actualResult == expectedResult
    }

    def "readMetadataForSender unhappy path works"() {
        given:
        mockDao.fetchMetadataForSender(_ as String) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabasePartnerMetadataStorage.getInstance().readMetadataForSender("TestSender")

        then:
        thrown(PartnerMetadataException)
    }
}
