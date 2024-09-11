package gov.hhs.cdc.trustedintermediary.rse2e

import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v251.datatype.HD
import ca.uhn.hl7v2.model.v251.segment.MSH
import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.HL7Message
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
            HL7Message inputMessage = messagePair.getKey() as HL7Message
            HL7Message outputMessage = messagePair.getValue() as HL7Message

            def value = inputMessage.get("MSH").getField(9, 0).getComponent(1)

            String statement = "MSH-4.1 = 'CDPH'"
            boolean result1 = HL7ExpressionEvaluator.parseAndEvaluate(inputMessage, outputMessage, statement)

            // TODO - based on MSH contents, cast message to more specific type like ORU, OML, etc?
            //            MSH inputMessageMSH = inputMessage.get("MSH") as MSH
            //            MSH outputMessageMSH = outputMessage.get("MSH") as MSH
            //            inputMessageMSH.getMessageControlID() == outputMessageMSH.getMessageControlID()

            //            def a = inputMessage.get("MSH").getField(6, 0).getComponent(1)
            //            def b = ((inputMessage.get("MSH") as MSH).getField(6, 0) as HD)
            //            def c = ((inputMessage.get("MSH") as MSH).getField(6, 0) as HD).getComponent(1)
        }
    }
}
