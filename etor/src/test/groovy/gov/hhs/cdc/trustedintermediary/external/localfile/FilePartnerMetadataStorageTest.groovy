package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

import java.time.Instant

class FilePartnerMetadataStorageTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(FilePartnerMetadataStorage, FilePartnerMetadataStorage.getInstance())
    }

    def "save and read metadata successfully"() {
        given:
        def expectedReceivedSubmissionId = "receivedSubmissionId"
        def expectedSentSubmissionId = "receivedSubmissionId"
        PartnerMetadata metadata = new PartnerMetadata(expectedReceivedSubmissionId, expectedSentSubmissionId, "sender", "receiver", Instant.parse("2023-12-04T18:51:48.941875Z"), "abcd")

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
        PartnerMetadata metadata = new PartnerMetadata("receivedSubmissionId", "sentSubmissionId","sender", "receiver", Instant.now(), "abcd")

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertToJsonString(_ as PartnerMetadata) >> {throw new FormatterProcessingException("error", new Exception())}
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        FilePartnerMetadataStorage.getInstance().saveMetadata(metadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "readMetadata throws PartnerMetadataException when unable to read file"() {
        given:
        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> {throw new FormatterProcessingException("error", new Exception())}
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        FilePartnerMetadataStorage.getInstance().readMetadata("receivedSubmissionId")

        then:
        thrown(PartnerMetadataException)
    }

    def "readMetadata returns empty when file does not exist"() {
        when:
        def actualMetadata = FilePartnerMetadataStorage.getInstance().readMetadata("nonexistentId")

        then:
        actualMetadata.isEmpty()
    }
}
