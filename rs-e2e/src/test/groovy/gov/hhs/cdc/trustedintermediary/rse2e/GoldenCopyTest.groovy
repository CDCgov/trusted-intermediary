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

class GoldenCopyTest extends Specification {

    List<HL7FileStream> azureFiles
    List<HL7FileStream> localFiles
    HapiHL7FileMatcher fileMatcher
    Logger mockLogger = Mock(Logger)
    List<String> loggedErrorsAndWarnings = []
    List<HapiHL7Message> failedFiles = []

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
        azureFiles = azureFileFetcher.fetchFiles(false)

        FileFetcher localFileFetcher = LocalFileFetcher.getInstance()
        localFiles = localFileFetcher.fetchFiles(false)
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

        //         Add a sub folder to /Golden for inputFile - this is so we can get the actual output, we won't use this
        //        Add a sub folder to /Golden for expectedFileOutput - this is 'hard-coded' and shouldnt change
        //         make sure to update folder paths in workflow

        given:
        def matchedFiles = fileMatcher.matchFiles(azureFiles, localFiles)

        when:
        for (filePair in matchedFiles) {
            def actualFile = filePair.getKey()
            def expectedFile = filePair.getValue()
            if (!actualFile.equals(expectedFile)) {
                failedFiles.add(expectedFile)
            }
        }

        then:
        assertTrue(failedFiles.isEmpty())
        if (!loggedErrorsAndWarnings.isEmpty()) {
            throw new AssertionError("Unexpected errors and/or warnings were logged:\n- ${loggedErrorsAndWarnings.join('\n- ')}")
        }
    }
}
