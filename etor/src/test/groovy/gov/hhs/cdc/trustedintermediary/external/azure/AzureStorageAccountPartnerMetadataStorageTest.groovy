package gov.hhs.cdc.trustedintermediary.external.azure

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import spock.lang.Specification

import java.time.Instant
import java.nio.charset.StandardCharsets

class AzureStorageAccountPartnerMetadataStorageTest extends Specification {

    def blobContainerClient
    def azureMetadataStorage

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        blobContainerClient = Mock(BlobContainerClient)
        azureMetadataStorage = new AzureStorageAccountPartnerMetadataStorage(blobContainerClient)
        TestApplicationContext.register(AzureStorageAccountPartnerMetadataStorage, azureMetadataStorage)
    }

    def "successfully read metadata"() {
        given: "A mock BlobClient and a successful download"
        def expectedUniqueId = "uniqueId"
        def expectedSender = "sender"
        def expectedReceiver = "receiver"
        def expectedTimestamp = Instant.parse("2023-12-04T18:51:48.941875Z")
        def expectedHash = "abcd"
        PartnerMetadata expectedMetadata = new PartnerMetadata(expectedUniqueId, expectedSender, expectedReceiver, expectedTimestamp, expectedHash)
        String content = '''{
            "uniqueId": "${expectedUniqueId}",
            "sender": "${expectedSender}",
            "receiver": "${expectedReceiver}",
            "timestamp": "${expectedTimestamp}",
            "hash": "${expectedHash}"
        }'''

        //        def storageService = AzureStorageAccountPartnerMetadataStorage.getInstance()
        String metadataFileName = azureMetadataStorage.getMetadataFileName(expectedUniqueId)

        //        def mockContainerClient = GroovyMock(BlobContainerClient)
        def mockBlobClient = Mock(BlobClient)
        mockBlobClient.downloadContent() >> content.getBytes(StandardCharsets.UTF_8)
        //        TestApplicationContext.register(BlobClient, mockBlobClient)

        //        def mockContainerClient = GroovyMock(BlobContainerClient)
        //        storageService.metaClass.getContainerClient >> { -> mockContainerClient }
        //        azureMetadataStorage.getContainerClient() >> mockContainerClient
        blobContainerClient.getBlobClient(metadataFileName) >> mockBlobClient
        //        TestApplicationContext.register(BlobContainerClient, mockContainerClient)

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when: "readMetadata is called"
        PartnerMetadata metadata = azureMetadataStorage.readMetadata(expectedUniqueId)

        then: "The metadata is returned correctly"
        metadata == expectedMetadata
    }
}
