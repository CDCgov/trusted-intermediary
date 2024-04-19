package gov.hhs.cdc.trustedintermediary.etor.metadata.partner

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType

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
        def timeDelivered = Instant.now()
        def hash = "abcd"
        def status = PartnerMetadataStatus.DELIVERED
        def failureReason = "failure reason"
        def messageType = PartnerMetadataMessageType.RESULT
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

        when:
        def metadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, timeDelivered, hash, PartnerMetadataStatus.DELIVERED, failureReason, messageType, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        then:
        metadata.receivedSubmissionId() == receivedSubmissionId
        metadata.sentSubmissionId() == sentSubmissionId
        metadata.sender() == sender
        metadata.receiver() == receiver
        metadata.timeDelivered() == timeDelivered
        metadata.timeReceived() == timeReceived
        metadata.hash() == hash
        metadata.deliveryStatus() == status
        metadata.failureReason() == failureReason
        metadata.sendingApplicationDetails() == sendingAppDetails
        metadata.sendingFacilityDetails() == sendingFacilityDetails
        metadata.receivingApplicationDetails() == receivingAppDetails
        metadata.receivingFacilityDetails() == receivingFacilityDetails
    }

    def "test overloaded constructor"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sender = "sender"
        def timeReceived = Instant.now()
        def timeDelivered = Instant.now()
        def hash = "abcd"
        def status = PartnerMetadataStatus.DELIVERED
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

        when:
        def metadata = new PartnerMetadata(receivedSubmissionId, timeReceived, timeDelivered, hash, PartnerMetadataStatus.DELIVERED, PartnerMetadataMessageType.ORDER, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        then:
        metadata.receivedSubmissionId() == receivedSubmissionId
        metadata.sentSubmissionId() == null
        metadata.timeReceived() == timeReceived
        metadata.timeDelivered() == timeDelivered
        metadata.hash() == hash
        metadata.deliveryStatus() == status
        metadata.sendingApplicationDetails() == sendingAppDetails
        metadata.sendingFacilityDetails() == sendingFacilityDetails
        metadata.receivingApplicationDetails() == receivingAppDetails
        metadata.receivingFacilityDetails() == receivingFacilityDetails
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
        metadata.timeReceived() == null
        metadata.timeDelivered() == null
        metadata.hash() == null
        metadata.deliveryStatus() == deliverStatus
    }

    def "test withDeliveryStatus to update PartnerMetadata"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "sender"
        def receiver = "receiver"
        def timeReceived = Instant.now()
        def timeDelivered = null
        def messageType = PartnerMetadataMessageType.RESULT
        def hash = "abcd"
        def failureReason = "DogCow goes boom"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def metadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, timeDelivered, hash, PartnerMetadataStatus.PENDING, failureReason, messageType, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        when:
        def newStatus = PartnerMetadataStatus.DELIVERED
        def updatedMetadata = metadata.withSentSubmissionId(sentSubmissionId).withDeliveryStatus(newStatus)

        then:
        updatedMetadata.receivedSubmissionId() == receivedSubmissionId
        updatedMetadata.sentSubmissionId() == sentSubmissionId
        updatedMetadata.sender() == sender
        updatedMetadata.receiver() == receiver
        updatedMetadata.timeReceived() == timeReceived
        updatedMetadata.timeDelivered() == null
        updatedMetadata.hash() == hash
        updatedMetadata.deliveryStatus() == newStatus
        updatedMetadata.failureReason() == failureReason
        updatedMetadata.sendingApplicationDetails() == sendingAppDetails
        updatedMetadata.sendingFacilityDetails() == sendingFacilityDetails
        updatedMetadata.receivingApplicationDetails() == receivingAppDetails
        updatedMetadata.receivingFacilityDetails() == receivingFacilityDetails
    }

    def "test withTimeDelivered to update PartnerMetadata"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "sender"
        def receiver = "receiver"
        def timeReceived = Instant.now()
        def timeDelivered = Instant.now()
        def messageType = PartnerMetadataMessageType.RESULT
        def hash = "abcd"
        def failureReason = "DogCow goes boom"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def metadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, null, hash, PartnerMetadataStatus.PENDING, failureReason, messageType, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        when:
        def updatedMetadata = metadata.withTimeDelivered(timeDelivered)

        then:
        updatedMetadata.receivedSubmissionId() == receivedSubmissionId
        updatedMetadata.sentSubmissionId() == sentSubmissionId
        updatedMetadata.sender() == sender
        updatedMetadata.receiver() == receiver
        updatedMetadata.timeReceived() == timeReceived
        updatedMetadata.timeDelivered() == timeDelivered
        updatedMetadata.hash() == hash
        updatedMetadata.failureReason() == failureReason
        updatedMetadata.sendingApplicationDetails() == sendingAppDetails
        updatedMetadata.sendingFacilityDetails() == sendingFacilityDetails
        updatedMetadata.receivingApplicationDetails() == receivingAppDetails
        updatedMetadata.receivingFacilityDetails() == receivingFacilityDetails
    }
}
