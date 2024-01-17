package gov.hhs.cdc.trustedintermediary.etor.metadata


import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus

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
        def status = PartnerMetadataStatus.DELIVERED
        def failureReason = "failure reason"


        when:
        def metadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timeReceived, hash, PartnerMetadataStatus.DELIVERED, failureReason)

        then:
        metadata.receivedSubmissionId() == receivedSubmissionId
        metadata.sentSubmissionId() == sentSubmissionId
        metadata.sender() == sender
        metadata.receiver() == receiver
        metadata.timeReceived() == timeReceived
        metadata.hash() == hash
        metadata.deliveryStatus() == status
        metadata.failureReason() == failureReason
    }

    def "test overloaded constructor"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sender = "sender"
        def timeReceived = Instant.now()
        def hash = "abcd"
        def status = PartnerMetadataStatus.DELIVERED

        when:
        def metadata = new PartnerMetadata(receivedSubmissionId, sender, timeReceived, hash, PartnerMetadataStatus.DELIVERED)

        then:
        metadata.receivedSubmissionId() == receivedSubmissionId
        metadata.sentSubmissionId() == null
        metadata.sender() == sender
        metadata.receiver() == null
        metadata.timeReceived() == timeReceived
        metadata.hash() == hash
        metadata.deliveryStatus() == status
    }

    def "test constructor with only received submission ID and hash"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def hash = "abcd"

        when:
        def metadata = new PartnerMetadata(receivedSubmissionId, hash)

        then:
        metadata.receivedSubmissionId() == receivedSubmissionId
        metadata.sentSubmissionId() == null
        metadata.sender() == null
        metadata.receiver() == null
        metadata.timeReceived() == null
        metadata.hash() == hash
        //Status should default to PENDING
        metadata.deliveryStatus() == PartnerMetadataStatus.PENDING
    }

    def "test constructor with only received submission ID and status"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def deliverStatus = PartnerMetadataStatus.DELIVERED

        when:
        def metadata = new PartnerMetadata(receivedSubmissionId, deliverStatus)

        then:
        metadata.receivedSubmissionId() == receivedSubmissionId
        metadata.sentSubmissionId() == null
        metadata.sender() == null
        metadata.receiver() == null
        metadata.timeReceived() == null
        metadata.hash() == null
        metadata.deliveryStatus() == deliverStatus
    }

    def "test withSentSubmissionId and withReceiver to update PartnerMetadata"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "sender"
        def receiver = "receiver"
        def timeReceived = Instant.now()
        def hash = "abcd"
        def status = PartnerMetadataStatus.DELIVERED
        def failureReason = "DogCow goes boom"
        def metadata = new PartnerMetadata(receivedSubmissionId, null, sender, null, timeReceived, hash, status, failureReason)

        when:
        def updatedMetadata = metadata.withSentSubmissionId(sentSubmissionId).withReceiver(receiver)

        then:
        updatedMetadata.receivedSubmissionId() == receivedSubmissionId
        updatedMetadata.sentSubmissionId() == sentSubmissionId
        updatedMetadata.sender() == sender
        updatedMetadata.receiver() == receiver
        updatedMetadata.timeReceived() == timeReceived
        updatedMetadata.hash() == hash
        updatedMetadata.deliveryStatus() == status
    }

    def "test withDeliveryStatus to update PartnerMetadata"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "sender"
        def receiver = "receiver"
        def timeReceived = Instant.now()
        def hash = "abcd"
        def failureReason = "DogCow goes boom"
        def metadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timeReceived, hash, PartnerMetadataStatus.PENDING, failureReason)

        when:
        def newStatus = PartnerMetadataStatus.DELIVERED
        def updatedMetadata = metadata.withSentSubmissionId(sentSubmissionId).withDeliveryStatus(newStatus)

        then:
        updatedMetadata.receivedSubmissionId() == receivedSubmissionId
        updatedMetadata.sentSubmissionId() == sentSubmissionId
        updatedMetadata.sender() == sender
        updatedMetadata.receiver() == receiver
        updatedMetadata.timeReceived() == timeReceived
        updatedMetadata.hash() == hash
        updatedMetadata.deliveryStatus() == newStatus
        updatedMetadata.failureReason() == failureReason
    }
}
