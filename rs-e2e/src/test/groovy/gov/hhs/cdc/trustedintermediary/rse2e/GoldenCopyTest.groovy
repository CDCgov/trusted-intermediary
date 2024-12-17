package gov.hhs.cdc.trustedintermediary.rse2e

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.rse2e.external.hapi.HapiHL7ExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.rse2e.external.hapi.HapiHL7FileMatcher
import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.AssertionRuleEngine
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class GoldenCopyTest extends Specification {

    List<HL7FileStream> azureFiles
    List<HL7FileStream> localFiles
    AssertionRuleEngine engine
    HapiHL7FileMatcher fileMatcher
    Logger mockLogger = Mock(Logger)
    List<String> loggedErrorsAndWarnings = []

    def setup() {
        fileMatcher =  HapiHL7FileMatcher.getInstance()

        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HapiHL7FileMatcher, fileMatcher)
        TestApplicationContext.register(HealthDataExpressionEvaluator, HapiHL7ExpressionEvaluator.getInstance())
        TestApplicationContext.register(LocalFileFetcher, LocalFileFetcher.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        mockLogger.logWarning(_ as String, _ as Object) >> { String msg, Object args ->
            loggedErrorsAndWarnings << msg
        }
        mockLogger.logError(_ as String, _ as Exception) >> { String msg, Exception e ->
            loggedErrorsAndWarnings << msg
        }

        FileFetcher azureFileFetcher = AzureBlobFileFetcher.getInstance()
        azureFiles = azureFileFetcher.fetchFiles()

        FileFetcher localFileFetcher = LocalFileFetcher.getInstance()
        localFiles = localFileFetcher.fetchFiles()
    }

    def cleanup() {
        for (HL7FileStream fileStream : localFiles + azureFiles) {
            fileStream.inputStream().close()
        }
    }

    def "Compare files"() {
        // notes:
        // Use workflow to sendoff the file (happens via Github Action)

        // Call the automated-staging-test-submit workflow. Prob use a variable for the path of the folder unless we add to same folder.
        // Call the automated-staging-test-run workflow. It shouldn't matter if it grabs all Azure files, we can use the MSH-10 to narrow it down like assertion_rules or use something else
        // Modify the Rules Engine or create a different one
        // If we do use the rules engine, see if its possible to compare the whole file as a rule rather than individual segments
        // Update Automated Test README

        // compare with known good file
        // def matchedFiles = fileMatcher.matchFiles(azureFiles, localFiles)

        given:
        def matchedFiles = fileMatcher.matchFiles(azureFiles, localFiles)

        when:
        for (messagePair in matchedFiles) {
            def inputMessage = messagePair.getKey()
            def outputMessage = messagePair.getValue()
            //            def evaluatedRules = engine.runRules(outputMessage, inputMessage)
            //            rulesToEvaluate.removeAll(evaluatedRules)
        }

        then:
        rulesToEvaluate.collect { it.name }.isEmpty() //Check whether all the rules in the assertions file have been run
        if (!loggedErrorsAndWarnings.isEmpty()) {
            throw new AssertionError("Unexpected errors and/or warnings were logged:\n- ${loggedErrorsAndWarnings.join('\n- ')}")
        }


        given:
        def beforeFileJsonFileString = Files.readString(Path.of("../examples/Test/Automated/golden_actual.hl7"))
        def afterFileJsonFileString = Files.readString(Path.of("../examples/Test/Automated/golden_expected.hl7"))


        when:
        def filesAreIdentical = beforeFileJsonFileString == afterFileJsonFileString

        then:
        assert filesAreIdentical, "test"
    }
}
