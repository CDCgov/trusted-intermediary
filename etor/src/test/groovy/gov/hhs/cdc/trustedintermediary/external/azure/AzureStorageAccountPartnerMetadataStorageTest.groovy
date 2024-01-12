package gov.hhs.cdc.trustedintermediary.external.azure

import com.azure.core.exception.AzureException
import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobClient
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import java.time.Instant
import spock.lang.Specification

class AzureStorageAccountPartnerMetadataStorageTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(AzureStorageAccountPartnerMetadataStorage, AzureStorageAccountPartnerMetadataStorage.getInstance())
    }

    def "successfully read metadata"() {
        given:
        def expectedReceivedSubmissionId = "receivedSubmissionId"
        def expectedSentSubmissionId = "sentSubmissionId"
        def expectedSender = "sender"
        def expectedReceiver = "receiver"
        def expectedTimestamp = Instant.parse("2023-12-04T18:51:48.941875Z")
        def expectedHash = "abcd"
        PartnerMetadataStatus status = PartnerMetadataStatus.PENDING

        PartnerMetadata expectedMetadata = new PartnerMetadata(expectedReceivedSubmissionId, expectedSentSubmissionId, expectedSender, expectedReceiver, expectedTimestamp, expectedHash, status)

        String simulatedMetadataJson = """{
            "receivedSubmissionId": "${expectedReceivedSubmissionId}",
            "sentSubmissionId": "${expectedSentSubmissionId}",
            "sender": "${expectedSender}",
            "receiver": "${expectedReceiver}",
            "timeReceived": "${expectedTimestamp}",
            "hash": "${expectedHash}",
            "deliveryStatus": "${status}"
        }"""

        def mockBlobClient = Mock(BlobClient)
        mockBlobClient.exists() >> true
        mockBlobClient.downloadContent() >> BinaryData.fromString(simulatedMetadataJson)

        def azureClient = Mock(AzureClient)
        azureClient.getBlobClient(_ as String) >> mockBlobClient

        TestApplicationContext.register(AzureClient, azureClient)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actualMetadata = AzureStorageAccountPartnerMetadataStorage.getInstance().readMetadata(expectedReceivedSubmissionId)

        then:
        actualMetadata.get() == expectedMetadata
    }

    def "readMetadata returns empty when blob does not exist"() {
        given:
        def mockBlobClient = Mock(BlobClient)
        mockBlobClient.exists() >> false

        def azureClient = Mock(AzureClient)
        azureClient.getBlobClient(_ as String) >> mockBlobClient

        TestApplicationContext.register(AzureClient, azureClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actualMetadata = AzureStorageAccountPartnerMetadataStorage.getInstance().readMetadata("nonexistentId")

        then:
        actualMetadata.isEmpty()
    }

    def "exception path while reading metadata"() {
        given:
        String expectedUniqueId = "receivedSubmissionId"
        def mockBlobClient = Mock(BlobClient)
        mockBlobClient.exists() >> true
        mockBlobClient.downloadContent() >> { throw new AzureException("Download error") }

        def azureClient = Mock(AzureClient)
        azureClient.getBlobClient(_ as String) >> mockBlobClient

        TestApplicationContext.register(AzureClient, azureClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        AzureStorageAccountPartnerMetadataStorage.getInstance().readMetadata(expectedUniqueId)

        then:
        thrown(PartnerMetadataException)
    }

    def "successfully save metadata"() {
        given:
        PartnerMetadata partnerMetadata = new PartnerMetadata("receivedSubmissionId", "sentSubmissionId", "sender", "receiver", Instant.now(), "abcd", PartnerMetadataStatus.DELIVERED)

        def mockBlobClient = Mock(BlobClient)
        def azureClient = Mock(AzureClient)
        azureClient.getBlobClient(_ as String) >> mockBlobClient

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertToJsonString(partnerMetadata) >> "DogCow"

        TestApplicationContext.register(AzureClient, azureClient)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        AzureStorageAccountPartnerMetadataStorage.getInstance().saveMetadata(partnerMetadata)

        then:
        1 * mockBlobClient.upload(_ as BinaryData, true)
    }

    def "failed to save metadata"() {
        given:
        PartnerMetadata partnerMetadata = new PartnerMetadata("receivedSubmissionId", "sentSubmissionId", "sender", "receiver", Instant.now(), "abcd", PartnerMetadataStatus.DELIVERED)

        def mockBlobClient = Mock(BlobClient)
        mockBlobClient.upload(_ as BinaryData, true) >> { throw new AzureException("upload failed") }

        def azureClient = Mock(AzureClient)
        azureClient.getBlobClient(_ as String) >> mockBlobClient

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertToJsonString(partnerMetadata) >> "DogCow"

        TestApplicationContext.register(AzureClient, azureClient)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        AzureStorageAccountPartnerMetadataStorage.getInstance().saveMetadata(partnerMetadata)

        then:
        thrown(PartnerMetadataException)
    }
}
