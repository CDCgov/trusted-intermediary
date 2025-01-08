package gov.hhs.cdc.trustedintermediary.rse2e

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7FileMatcher
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7FileStream
import gov.hhs.cdc.trustedintermediary.rse2e.external.localfile.LocalFileFetcher
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7ExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7Message
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import spock.lang.Specification
import gov.hhs.cdc.trustedintermediary.rse2e.external.azure.AzureBlobFileFetcher

class GoldenCopyTest extends Specification {

    List<HL7FileStream> azureFiles
    List<HL7FileStream> localFiles
    HL7FileMatcher fileMatcher
    Logger mockLogger = Mock(Logger)
    List<String> loggedErrorsAndWarnings = []
    List<String> failedFiles = []

    def setup() {
        fileMatcher =  HL7FileMatcher.getInstance()

        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HL7FileMatcher, fileMatcher)
        TestApplicationContext.register(HealthDataExpressionEvaluator, HL7ExpressionEvaluator.getInstance())
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
        // Currently reusing automated staging test workflows but we might need to pivot for cron schedule

        given:
        def matchedFiles = fileMatcher.matchFiles(azureFiles, localFiles)

        when:
        for (filePair in matchedFiles) {
            def actualFile = filePair.getKey()
            def expectedFile = filePair.getValue()
            if (actualFile.toString() != expectedFile.toString()) {
                failedFiles.add(expectedFile.getIdentifier())
                System.out.println("This guy didnt match: " + expectedFile.getIdentifier())
            }
            else {
                System.out.println("This guy matched: " + expectedFile.getIdentifier())
            }
        }

        then:
        assert failedFiles.isEmpty()
        if (!loggedErrorsAndWarnings.isEmpty()) {
            throw new AssertionError("Unexpected errors and/or warnings were logged:\n- ${loggedErrorsAndWarnings.join('\n- ')}")
        }
    }
}
