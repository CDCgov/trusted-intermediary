package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import java.nio.file.Files
import java.time.Instant
import spock.lang.Specification

class FilePartnerMetadataStorageTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(FilePartnerMetadataStorage, FilePartnerMetadataStorage.getInstance())

        Files.list(FilePartnerMetadataStorage.METADATA_DIRECTORY).forEach {Files.delete(it) }
    }

    def "save and read metadata successfully"() {
        given:
        def expectedReceivedSubmissionId = "receivedSubmissionId"
        def expectedSentSubmissionId = "receivedSubmissionId"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        PartnerMetadata metadata = new PartnerMetadata(expectedReceivedSubmissionId, expectedSentSubmissionId, "sender", "receiver", Instant.parse("2023-12-04T18:51:48.941875Z"),Instant.parse("2023-12-04T18:51:48.941875Z"), "abcd", PartnerMetadataStatus.DELIVERED, null, PartnerMetadataMessageType.ORDER, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata)
        def actualMetadata = FilePartnerMetadataStorage.getInstance().readMetadata(expectedReceivedSubmissionId)

        then:
        actualMetadata.get() == metadata
    }

    def "saveMetadata throws PartnerMetadataException when unable to save file"() {
        given:
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        PartnerMetadata metadata = new PartnerMetadata("receivedSubmissionId", "sentSubmissionId","sender", "receiver", Instant.now(), Instant.now(), "abcd", PartnerMetadataStatus.DELIVERED, null, PartnerMetadataMessageType.ORDER, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertToJsonString(_ as PartnerMetadata) >> {throw new FormatterProcessingException("error", new Exception())}
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "readMetadata throws PartnerMetadataException when unable to parse file"() {
        given:
        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> {throw new FormatterProcessingException("error", new Exception())}
        mockFormatter.convertToJsonString(_) >> "DogCow is great!"
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        //write something to the hard drive so that the `readMetadata` in the when gets pass the file existence check
        def submissionId = "asljfaskljgalsjgjlas"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        PartnerMetadata metadata = new PartnerMetadata(submissionId, null, null, null, null, null, null, null, null, null, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata)

        when:
        FilePartnerMetadataStorage.getInstance().readMetadata(submissionId)

        then:
        thrown(PartnerMetadataException)
    }

    def "readMetadata returns empty when file does not exist"() {
        when:
        def actualMetadata = FilePartnerMetadataStorage.getInstance().readMetadata("nonexistentId")

        then:
        actualMetadata.isEmpty()
    }

    def "readMetadataForSender returns a set of PartnerMetadata"() {
        given:
        def sender = "same_sender"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        PartnerMetadata metadata2 = new PartnerMetadata("abcdefghi", null, sender, null, null, null, null, null, null, null, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")
        PartnerMetadata metadata1 = new PartnerMetadata("123456789", null, sender, null, null, null, null, null, null, null, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata1)
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata2)
        def metadataSet = FilePartnerMetadataStorage.getInstance().readMetadataForSender(sender)

        then:
        metadataSet.containsAll(Set.of(metadata1, metadata2))
    }

    def "readMetadataForMessageLinking returns a set of PartnerMetadata"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def placerOrderNumber = "placerOrderNumber"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        PartnerMetadata metadata1 = new PartnerMetadata(receivedSubmissionId, null, null, null, null, null, null, null, null, null, sendingAppDetails, sendingFacilityDetails, null, null, placerOrderNumber)
        PartnerMetadata metadata2 = new PartnerMetadata("2", null, null, null, null, null, null, null, null, null, sendingAppDetails, sendingFacilityDetails, null, null, placerOrderNumber)

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata1)
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata2)
        def metadataSet = FilePartnerMetadataStorage.getInstance().readMetadataForMessageLinking(receivedSubmissionId)

        then:
        metadataSet.containsAll(Set.of(metadata1, metadata2))
    }

    def "readMetadataForMessageLinking returns an empty set when no metadata is found"() {
        when:
        def metadataSet = FilePartnerMetadataStorage.getInstance().readMetadataForMessageLinking("nonexistentId")

        then:
        metadataSet.isEmpty()
    }

    def "readMetadataForMessageLinking throws PartnerMetadataException when unable to parse file"() {
        given:
        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> {throw new FormatterProcessingException("error", new Exception())}
        mockFormatter.convertToJsonString(_) >> "[]"
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        def submissionId = "submissionId"
        PartnerMetadata metadata = new PartnerMetadata(submissionId, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata)

        when:
        FilePartnerMetadataStorage.getInstance().readMetadataForMessageLinking("submissionId")

        then:
        thrown(PartnerMetadataException)
    }
}
