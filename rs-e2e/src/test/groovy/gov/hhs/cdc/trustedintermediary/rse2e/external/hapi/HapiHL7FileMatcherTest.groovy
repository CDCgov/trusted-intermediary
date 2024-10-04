package gov.hhs.cdc.trustedintermediary.rse2e.external.hapi

import ca.uhn.hl7v2.model.Message
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.rse2e.HL7FileStream
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class HapiHL7FileMatcherTest extends Specification {

    def mockLogger = Mock(Logger)
    def fileMatcher = HapiHL7FileMatcher.getInstance()

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(HapiHL7FileMatcher, fileMatcher)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "should correctly match input and output files and log unmatched files"() {
        given:
        def spyFileMatcher = Spy(HapiHL7FileMatcher.getInstance())
        def fileStream1 = new HL7FileStream("file1", Mock(InputStream))
        def fileStream2 = new HL7FileStream("file2", Mock(InputStream))
        def fileStream3 = new HL7FileStream("file3", Mock(InputStream))
        def mockInputFiles = [fileStream1, fileStream2]
        def mockOutputFiles = [fileStream2, fileStream3]
        def mockInputMessage2 = Mock(Message)
        def mockOutputMessage2 = Mock(Message)
        spyFileMatcher.mapMessageByControlId(mockInputFiles) >> [ "1": Mock(Message), "2": mockInputMessage2 ]
        spyFileMatcher.mapMessageByControlId(mockOutputFiles) >> [ "2": mockOutputMessage2, "3": Mock(Message) ]

        when:
        def result = spyFileMatcher.matchFiles(mockOutputFiles, mockInputFiles)

        then:
        result.size() == 1
        result == Map.of(mockInputMessage2, mockOutputMessage2)
        1 * mockLogger.logError({ it.contains("Found no match") && it.contains("1") && it.contains("3") })
    }

    def "should map message by control ID"() {
        given:
        def msh1to9 = "MSH|^~\\&|Sender Application^sender.test.com^DNS|Sender Facility^0.0.0.0.0.0.0.0^ISO|Receiver Application^0.0.0.0.0.0.0.0^ISO|Receiver Facility^simulated-lab-id^DNS|20230101010000-0000||ORM^O01^ORM_O01|"
        def msh11to12 = "|T|2.5.1"
        def file1Msh10 = "1111111"
        String file1MshSegment = msh1to9 + file1Msh10 + msh11to12
        def file1InputStream = new ByteArrayInputStream(file1MshSegment.bytes)
        def file1Hl7FileStream = new HL7FileStream("file1", file1InputStream)
        def file2Msh10 = "2222222"
        String file2MshSegment = msh1to9 + file2Msh10 + msh11to12
        def file2InputStream = new ByteArrayInputStream(file2MshSegment.bytes)
        def file2Hl7FileStream = new HL7FileStream("file2", file2InputStream)
        def mockFiles = [
            file1Hl7FileStream,
            file2Hl7FileStream
        ]

        when:
        def result = fileMatcher.mapMessageByControlId(mockFiles)

        then:
        result.size() == 2
        result[file1Msh10] != null
        file1MshSegment == result[file1Msh10].encode().trim()
        result[file2Msh10] != null
        file2MshSegment == result[file2Msh10].encode().trim()
    }

    def "should log an error and continue when MSH-10 is empty"() {
        given:
        def msh1to9 = "MSH|^~\\&|Sender Application^sender.test.com^DNS|Sender Facility^0.0.0.0.0.0.0.0^ISO|Receiver Application^0.0.0.0.0.0.0.0^ISO|Receiver Facility^simulated-lab-id^DNS|20230101010000-0000||ORM^O01^ORM_O01|"
        def msh11to12 = "|T|2.5.1"
        def emptyMsh10 = ""
        String mshSegment = msh1to9 + emptyMsh10 + msh11to12
        def inputStream = new ByteArrayInputStream(mshSegment.bytes)
        def hl7FileStream = new HL7FileStream("file1", inputStream)

        when:
        def result = fileMatcher.mapMessageByControlId([hl7FileStream])

        then:
        result.size() == 0
        1 * mockLogger.logError({ it.contains("MSH-10 is empty") })
    }

    def "should log an error when not able to parse the file as HL7 message"() {
        given:
        def inputStream = new ByteArrayInputStream("".bytes)
        def hl7FileStream = new HL7FileStream("badFile", inputStream)

        when:
        def result = fileMatcher.mapMessageByControlId([hl7FileStream])

        then:
        result.size() == 0
        1 * mockLogger.logError({ it.contains("An error occurred while parsing the message") }, _ as Exception)
    }
}
