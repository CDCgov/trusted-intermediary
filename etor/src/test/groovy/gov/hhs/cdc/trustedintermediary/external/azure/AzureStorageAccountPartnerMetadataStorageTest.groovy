package gov.hhs.cdc.trustedintermediary.external.azure

import com.azure.core.exception.AzureException
import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobClient
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException
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
        given: "A mock BlobClient and a successful download"
        def expectedUniqueId = "uniqueId"
        def expectedSender = "sender"
        def expectedReceiver = "receiver"
        def expectedTimestamp = Instant.parse("2023-12-04T18:51:48.941875Z")
        def expectedHash = "abcd"

        PartnerMetadata expectedMetadata = new PartnerMetadata(expectedUniqueId, expectedSender, expectedReceiver, expectedTimestamp, expectedHash)

        String simulatedMetadataJson = """{
            "uniqueId": "${expectedUniqueId}",
            "sender": "${expectedSender}",
            "receiver": "${expectedReceiver}",
            "timeReceived": "${expectedTimestamp}",
            "hash": "${expectedHash}"
        }"""

        def mockBlobClient = Mock(BlobClient)
        mockBlobClient.downloadContent() >> BinaryData.fromString(simulatedMetadataJson)

        def azureClient = Mock(AzureClient)
        azureClient.getBlobClient(_ as String) >> mockBlobClient

        TestApplicationContext.register(AzureClient, azureClient)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadata actualMetadata = AzureStorageAccountPartnerMetadataStorage.getInstance().readMetadata(expectedUniqueId)

        then:
        actualMetadata == expectedMetadata
    }

    def "exception path while reading metadata"() {
        given:
        String expectedUniqueId = "uniqueId"
        def mockBlobClient = Mock(BlobClient)
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
}
