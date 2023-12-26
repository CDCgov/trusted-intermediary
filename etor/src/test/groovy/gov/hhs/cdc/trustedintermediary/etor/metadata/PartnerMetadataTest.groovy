package gov.hhs.cdc.trustedintermediary.etor.metadata


import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import java.time.Instant
import spock.lang.Specification

class PartnerMetadataTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(PartnerMetadata)

        then:
        noExceptionThrown()
    }

    def "test constructor"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "sender"
        def receiver = "receiver"
        def timeReceived = Instant.now()
        def hash = "abcd"

        when:
        def metadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timeReceived, hash)

        then:
        metadata.receivedSubmissionId() == receivedSubmissionId
        metadata.sentSubmissionId() == sentSubmissionId
        metadata.sender() == sender
        metadata.receiver() == receiver
        metadata.timeReceived() == timeReceived
        metadata.hash() == hash
    }
}
