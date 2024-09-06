package gov.hhs.cdc.trustedintermediary.rse2e

import com.azure.storage.blob.BlobClient
import spock.lang.Specification

class AutomatedTest  extends Specification  {

    def recentFiles

    def setup() {
        FileFetcher<BlobClient> fetcher = new AzureBlobFileFetcher()
        recentFiles = fetcher.fetchFiles()
    }


    def "testing something"() {
        given:

        when:
        for (BlobClient file : recentFiles) {
            System.out.println("File: " + file.getBlobName())
        }

        then:
        true
    }
}
