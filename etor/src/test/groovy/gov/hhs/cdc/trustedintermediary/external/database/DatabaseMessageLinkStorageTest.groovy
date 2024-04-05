package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException
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
class DatabaseMessageLinkStorageTest extends Specification {

    private def mockDao

    def mockMessageLinkData = new MessageLink(1, "TestMessageId")

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockDao = Mock(DbDao)

        TestApplicationContext.register(DbDao, mockDao)
        TestApplicationContext.register(DatabaseMessageLinkStorage, DatabaseMessageLinkStorage.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "getMessageLink happy path works"() {
        given:
        def expectedResult = Optional.of(mockMessageLinkData)

        mockDao.fetchMessageLink(_ as String) >> Optional.of(mockMessageLinkData)

        when:
        def actualResult = DatabaseMessageLinkStorage.getInstance().getMessageLink("TestSubmissionId")

        then:
        actualResult == expectedResult
    }

    def "getMessageLink unhappy path works"() {
        given:
        mockDao.fetchMessageLink(_ as String) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabaseMessageLinkStorage.getInstance().getMessageLink("TestSubmissionId")

        then:
        thrown(Exception)
    }

    def "saveLinkedMessages happy path works"() {
        when:
        DatabaseMessageLinkStorage.getInstance().saveMessageLink(mockMessageLinkData)

        then:
        1 * mockDao.insertMessageLink(mockMessageLinkData)
    }

    def "saveMessageLink unhappy path works"() {
        given:
        mockDao.insertMessageLink(_ as MessageLink) >> { throw new SQLException("Something went wrong!") }

        when:
        DatabaseMessageLinkStorage.getInstance().saveMessageLink(mockMessageLinkData)

        then:
        thrown(MessageLinkException)
    }
}
