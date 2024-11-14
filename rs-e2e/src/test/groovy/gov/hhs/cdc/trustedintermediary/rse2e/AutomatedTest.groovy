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

    List<HL7FileStream> azureFiles
    List<HL7FileStream> localFiles
    AssertionRuleEngine engine
    HapiHL7FileMatcher fileMatcher
    Logger mockLogger = Mock(Logger)

    def setup() {
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
        TestApplicationContext.register(LocalFileFetcher, LocalFileFetcher.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        FileFetcher azureFileFetcher = AzureBlobFileFetcher.getInstance()
        azureFiles = azureFileFetcher.fetchFiles()

        FileFetcher localFileFetcher = LocalFileFetcher.getInstance()
        localFiles = localFileFetcher.fetchFiles()

        engine.ensureRulesLoaded()
    }

    def cleanup() {
        for (HL7FileStream fileStream : localFiles + azureFiles) {
            fileStream.inputStream().close()
        }
    }

    def "test defined assertions on relevant messages"() {
        given:
        def toRunRules = engine.getRules()
        def matchedFiles = fileMatcher.matchFiles(azureFiles, localFiles)

        when:
        for (messagePair in matchedFiles) {
            Message inputMessage = messagePair.getKey() as Message
            Message outputMessage = messagePair.getValue() as Message
            def runRules = engine.runRules(outputMessage, inputMessage)
            toRunRules.removeAll(runRules)
        }

        then:
        toRunRules.collect { it.name }.isEmpty()
        0 * mockLogger.logError(_ as String, _ as Exception)
        0 * mockLogger.logWarning(_ as String)
    }
}
