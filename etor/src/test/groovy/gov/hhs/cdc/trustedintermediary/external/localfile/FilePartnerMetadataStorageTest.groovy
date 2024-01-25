package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
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
        PartnerMetadata metadata = new PartnerMetadata(expectedReceivedSubmissionId, expectedSentSubmissionId, "sender", "receiver", Instant.parse("2023-12-04T18:51:48.941875Z"), "abcd", PartnerMetadataStatus.DELIVERED, null)

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
        PartnerMetadata metadata = new PartnerMetadata("receivedSubmissionId", "sentSubmissionId","sender", "receiver", Instant.now(), "abcd", PartnerMetadataStatus.DELIVERED, null)

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
        PartnerMetadata metadata = new PartnerMetadata(submissionId, null, null, null, null, null, null, null)
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
        PartnerMetadata metadata = new PartnerMetadata("123456789", null, "sender", null, null, null, null, null)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata)
        def metadataSet = FilePartnerMetadataStorage.getInstance().readMetadataForSender("sender")

        then:
        metadataSet[0].sender() == "sender"
    }
}
