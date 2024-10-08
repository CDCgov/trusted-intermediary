package gov.hhs.cdc.trustedintermediary.rse2e

import ca.uhn.hl7v2.model.Message
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.rse2e.external.hapi.HapiHL7FileMatcher
import gov.hhs.cdc.trustedintermediary.rse2e.external.hapi.HapiHL7ExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.AssertionRuleEngine
import spock.lang.Specification

class AutomatedTest extends Specification  {

    List<HL7FileStream> recentAzureFiles
    List<HL7FileStream> recentLocalFiles
    AssertionRuleEngine engine
    HapiHL7FileMatcher fileMatcher
    def mockLogger = Mock(Logger)

    def setup() {
        FileFetcher azureFileFetcher = AzureBlobFileFetcher.getInstance()
        recentAzureFiles = azureFileFetcher.fetchFiles()

        FileFetcher localFileFetcher = LocalFileFetcher.getInstance()
        recentLocalFiles = localFileFetcher.fetchFiles()

        engine = AssertionRuleEngine.getInstance()
        fileMatcher =  HapiHL7FileMatcher.getInstance()

        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(AssertionRuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HapiHL7FileMatcher, fileMatcher)
        TestApplicationContext.register(HealthDataExpressionEvaluator, HapiHL7ExpressionEvaluator.getInstance())
        TestApplicationContext.register(AzureBlobFileFetcher, azureFileFetcher)
        TestApplicationContext.register(LocalFileFetcher, LocalFileFetcher.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def cleanup() {
        for (HL7FileStream fileStream : recentLocalFiles + recentAzureFiles) {
            fileStream.inputStream().close()
        }
    }

    def "test defined assertions on relevant messages"() {
        given:
        def matchedFiles = fileMatcher.matchFiles(recentAzureFiles, recentLocalFiles)

        when:
        for (messagePair in matchedFiles) {
            Message inputMessage = messagePair.getKey() as Message
            Message outputMessage = messagePair.getValue() as Message
            engine.runRules(outputMessage, inputMessage)
        }

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        0 * mockLogger.logWarning(_ as String)
    }
}
