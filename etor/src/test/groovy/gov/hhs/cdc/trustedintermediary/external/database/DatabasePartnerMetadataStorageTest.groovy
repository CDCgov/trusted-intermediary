package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import spock.lang.Specification

import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant

class DatabasePartnerMetadataStorageTest extends Specification {

    private def mockDao

    def mockMetadata = new PartnerMetadata(
    "receivedSubmissionId",
    "sentSubmissionId",
    "sender",
    "receiver",
    Instant.now(),
    Instant.now(),
    "hash",
    PartnerMetadataStatus.PENDING,
    "DogCow failure",
    PartnerMetadataMessageType.RESULT
    )

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
                new DbColumn("message_type", mockMetadata.messageType().toString(), false, Types.OTHER)
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
                null
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
                new DbColumn("message_type", null, false, Types.OTHER)
                )

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        1 * mockDao.upsertData("metadata", columns, "received_message_id")
    }
}
