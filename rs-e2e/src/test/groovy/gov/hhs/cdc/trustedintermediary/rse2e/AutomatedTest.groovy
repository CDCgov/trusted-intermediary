package gov.hhs.cdc.trustedintermediary.rse2e

import ca.uhn.hl7v2.model.Message
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.AssertionRuleEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.RuleLoader
import spock.lang.Specification

class AutomatedTest  extends Specification  {

    List<InputStream> recentAzureFiles
    List<InputStream> recentLocalFiles
    AssertionRuleEngine engine
    def mockLogger = Mock(Logger)

    def setup() {
        FileFetcher azureFileFetcher = new AzureBlobFileFetcher()
        recentAzureFiles = azureFileFetcher.fetchFiles()

        FileFetcher localFileFetcher = new LocalFileFetcher()
        recentLocalFiles = localFileFetcher.fetchFiles()

        engine = new AssertionRuleEngine()

        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(AssertionRuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        // Figure out env vars (need Azure connection string)
        // Also add unit tests for file matcher
        // TODO - fix MSH-9 segment to match spec
    }


    def "test defined assertions on relevant messages"() {
        given:
        def matchedFiles = HL7FileMatcher.matchFiles(recentAzureFiles, recentLocalFiles)

        when:
        for (messagePair in matchedFiles) {
            Message inputMessage = messagePair.getKey() as Message
            Message outputMessage = messagePair.getValue() as Message
            engine.runRules(outputMessage, inputMessage)
        }

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
    }
}
