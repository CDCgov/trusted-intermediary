package gov.hhs.cdc.trustedintermediary.rse2e

import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v251.segment.MSH
import spock.lang.Specification

class AutomatedTest  extends Specification  {

    List<InputStream> recentAzureFiles
    List<InputStream> recentLocalFiles

    def setup() {
        FileFetcher azureFileFetcher = new AzureBlobFileFetcher()
        recentAzureFiles = azureFileFetcher.fetchFiles()

        FileFetcher localFileFetcher = new LocalFileFetcher()
        recentLocalFiles = localFileFetcher.fetchFiles()

        // link files by MSH-10
        // assert matching fields in local vs azure files
        // some asserts will be the same for all files, some asserts will be partner-specific
        //      and/or message-type specific

        // Create file matcher
        // Figure out env vars (need Azure connection string)
        // Use file matcher in automated test
        // Also add unit tests for file matcher

        // TODO - fix MSH-9 segment to match spec
    }


    def "testing something"() {
        given:

        when:
        def matchedFiles = HL7FileMatcher.matchFiles(recentAzureFiles, recentLocalFiles)

        then:
        for (messagePair in matchedFiles) {
            Message inputMessage = messagePair.getKey()
            Message outputMessage = messagePair.getValue()

            // TODO - based on MSH contents, cast message to more specific type like ORU, OML, etc?
            MSH inputMessageMSH = inputMessage.get("MSH")
            MSH outputMessageMSH = outputMessage.get("MSH")
            inputMessageMSH.getMessageControlID() == outputMessageMSH.getMessageControlID()
        }
    }
}