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

    def "test overloaded constructor"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sender = "sender"
        def timeReceived = Instant.now()
        def hash = "abcd"

        when:
        def metadata = new PartnerMetadata(receivedSubmissionId, sender, timeReceived, hash)

        then:
        metadata.receivedSubmissionId() == receivedSubmissionId
        metadata.sentSubmissionId() == null
        metadata.sender() == sender
        metadata.receiver() == null
        metadata.timeReceived() == timeReceived
        metadata.hash() == hash
    }

    def "test withSentSubmissionId and withReceiver to update PartnerMetadata"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "sender"
        def receiver = "receiver"
        def timeReceived = Instant.now()
        def hash = "abcd"
        def metadata = new PartnerMetadata(receivedSubmissionId, sender, timeReceived, hash)

        when:
        def updatedMetadata = metadata.withSentSubmissionId(sentSubmissionId).withReceiver(receiver)

        then:
        updatedMetadata.receivedSubmissionId() == receivedSubmissionId
        updatedMetadata.sentSubmissionId() == sentSubmissionId
        updatedMetadata.sender() == sender
        updatedMetadata.receiver() == receiver
        updatedMetadata.timeReceived() == timeReceived
        updatedMetadata.hash() == hash
    }
}
