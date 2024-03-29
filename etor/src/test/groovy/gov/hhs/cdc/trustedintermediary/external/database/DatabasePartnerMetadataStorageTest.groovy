package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import spock.lang.Specification

import java.sql.SQLException
import java.time.Instant

class DatabasePartnerMetadataStorageTest extends Specification {

    private def mockDao

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
        def receivedSubmissionId = "receivedSubmissionId"
        def messageType = PartnerMetadataMessageType.RESULT
        def mockMetadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", "receiver", Instant.now(), Instant.now(), "hash", PartnerMetadataStatus.PENDING, null, messageType)
        def expectedResult = Optional.of(mockMetadata)

        mockDao.fetchMetadata(_ as String) >> mockMetadata

        when:
        def actualResult = DatabasePartnerMetadataStorage.getInstance().readMetadata(receivedSubmissionId)

        then:
        actualResult == expectedResult
    }

    def "readMetadata unhappy path works"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        mockDao.fetchMetadata(_ as String) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabasePartnerMetadataStorage.getInstance().readMetadata(receivedSubmissionId)

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata happy path works"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def messageType = PartnerMetadataMessageType.RESULT
        def mockMetadata = new PartnerMetadata(
                receivedSubmissionId,
                "sentSubmissionId",
                "sender",
                "receiver",
                Instant.now(),
                Instant.now(),
                "hash",
                PartnerMetadataStatus.PENDING,
                "DogCow failure",
                messageType
                )

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        1 * mockDao.upsertMetadata(
                mockMetadata.receivedSubmissionId(),
                mockMetadata.sentSubmissionId(),
                mockMetadata.sender(),
                mockMetadata.receiver(),
                mockMetadata.hash(),
                mockMetadata.timeReceived(),
                mockMetadata.timeDelivered(),
                mockMetadata.deliveryStatus(),
                mockMetadata.failureReason(),
                mockMetadata.messageType()
                )
    }

    def "saveMetadata unhappy path works"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def messageType = PartnerMetadataMessageType.ORDER
        def mockMetadata = new PartnerMetadata(
                receivedSubmissionId,
                "sentSubmissionId",
                "sender",
                "receiver",
                Instant.now(),
                Instant.now(),
                "hash",
                PartnerMetadataStatus.FAILED,
                "DogCow failure",
                messageType
                )

        mockDao.upsertMetadata(
                mockMetadata.receivedSubmissionId(),
                mockMetadata.sentSubmissionId(),
                mockMetadata.sender(),
                mockMetadata.receiver(),
                mockMetadata.hash(),
                mockMetadata.timeReceived(),
                mockMetadata.timeDelivered(),
                mockMetadata.deliveryStatus(),
                mockMetadata.failureReason(),
                mockMetadata.messageType()
                ) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "saveMetadata writes null timestamp"() {
    }
}
