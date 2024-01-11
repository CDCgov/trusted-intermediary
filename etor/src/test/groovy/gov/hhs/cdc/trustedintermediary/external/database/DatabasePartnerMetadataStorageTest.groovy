package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage
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
        def mockMetadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", "receiver", Instant.now(), "hash")
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
        def mockMetadata = new PartnerMetadata(
                receivedSubmissionId,
                "sentSubmissionId",
                "sender",
                "receiver",
                Instant.now(),
                "hash"
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
                mockMetadata.timeReceived()
                )
    }

    def "saveMetadata unhappy path works"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def mockMetadata = new PartnerMetadata(
                receivedSubmissionId,
                "sentSubmissionId",
                "sender",
                "receiver",
                Instant.now(),
                "hash"
                )

        mockDao.upsertMetadata(
                mockMetadata.receivedSubmissionId(),
                mockMetadata.sentSubmissionId(),
                mockMetadata.sender(),
                mockMetadata.receiver(),
                mockMetadata.hash(),
                mockMetadata.timeReceived()
                ) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabasePartnerMetadataStorage.getInstance().saveMetadata(mockMetadata)

        then:
        thrown(PartnerMetadataException)
    }
}
