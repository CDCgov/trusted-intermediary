package gov.hhs.cdc.trustedintermediary.rse2e.hl7

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class HL7FileMatcherTest extends Specification {

    def mockLogger = Mock(Logger)
    def fileMatcher = HL7FileMatcher.getInstance()

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(HL7FileMatcher, fileMatcher)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "should correctly match input and output files"() {
        given:
        def spyFileMatcher = Spy(HL7FileMatcher.getInstance())
        def mockInputFiles = [
            new HL7FileStream("inputFileStream1", Mock(InputStream)),
            new HL7FileStream("inputFileStream2", Mock(InputStream))
        ]
        def mockOutputFiles = [
            new HL7FileStream("outputFileStream1", Mock(InputStream)),
            new HL7FileStream("outputFileStream2", Mock(InputStream))
        ]
        def mockInputMessage1 = Mock(HL7Message)
        def mockInputMessage2 = Mock(HL7Message)
        def mockOutputMessage1 = Mock(HL7Message)
        def mockOutputMessage2 = Mock(HL7Message)
        spyFileMatcher.parseAndMapMessageByControlId(mockInputFiles) >> ["001": mockInputMessage1, "002": mockInputMessage2 ]
        spyFileMatcher.parseAndMapMessageByControlId(mockOutputFiles) >> ["001": mockOutputMessage1, "002": mockOutputMessage2 ]

        when:
        def result = spyFileMatcher.matchFiles(mockOutputFiles, mockInputFiles)

        then:
        result.size() == 2
        result == Map.of(mockInputMessage1, mockOutputMessage1, mockInputMessage2, mockOutputMessage2)
    }


    def "should throw HL7FileMatcherException if didn't find a match for at least one file in either input or output"() {
        given:
        def mockInputFiles
        def mockOutputFiles
        def spyFileMatcher = Spy(HL7FileMatcher.getInstance())

        when:
        mockInputFiles = [
            new HL7FileStream("nonMatchingInputFileStream", Mock(InputStream)),
            new HL7FileStream("matchingInputFileStream", Mock(InputStream))
        ]
        mockOutputFiles = [
            new HL7FileStream("matchingOutputFileStream", Mock(InputStream))
        ]
        spyFileMatcher.parseAndMapMessageByControlId(mockInputFiles) >> ["001": Mock(HL7Message), "002": Mock(HL7Message) ]
        spyFileMatcher.parseAndMapMessageByControlId(mockOutputFiles) >> ["001": Mock(HL7Message) ]
        spyFileMatcher.matchFiles(mockOutputFiles, mockInputFiles)

        then:
        def nonMatchingInputException = thrown(HL7FileMatcherException)
        with(nonMatchingInputException.getMessage()) {
            contains("Found no match")
            contains("002")
        }

        when:
        mockInputFiles = [
            new HL7FileStream("matchingInputFileStream", Mock(InputStream))
        ]
        mockOutputFiles = [
            new HL7FileStream("matchingOutputFileStream", Mock(InputStream)),
            new HL7FileStream("nonMatchingOutputFileStream", Mock(InputStream))
        ]
        spyFileMatcher.parseAndMapMessageByControlId(mockInputFiles) >> ["001": Mock(HL7Message) ]
        spyFileMatcher.parseAndMapMessageByControlId(mockOutputFiles) >> ["001": Mock(HL7Message), "003": Mock(HL7Message) ]
        spyFileMatcher.matchFiles(mockOutputFiles, mockInputFiles)

        then:
        def nonMatchingOutputException = thrown(HL7FileMatcherException)
        with(nonMatchingOutputException.getMessage()) {
            contains("Found no match")
            contains("003")
        }
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
        def result = fileMatcher.parseAndMapMessageByControlId(mockFiles)

        then:
        result.size() == 2
        def message1 = result[file1Msh10]
        def message2 = result[file2Msh10]
        message1 != null
        message2 != null
        file1MshSegment == message1.getUnderlyingData().toString()
        file2MshSegment == message2.getUnderlyingData().toString()
    }

    def "should throw HL7FileMatcherException when MSH-10 is empty"() {
        given:
        def msh1to9 = "MSH|^~\\&|Sender Application^sender.test.com^DNS|Sender Facility^0.0.0.0.0.0.0.0^ISO|Receiver Application^0.0.0.0.0.0.0.0^ISO|Receiver Facility^simulated-lab-id^DNS|20230101010000-0000||ORM^O01^ORM_O01|"
        def msh11to12 = "|T|2.5.1"
        def emptyMsh10 = ""
        String mshSegment = msh1to9 + emptyMsh10 + msh11to12
        def inputStream = new ByteArrayInputStream(mshSegment.bytes)
        def hl7FileStream = new HL7FileStream("file1", inputStream)

        when:
        fileMatcher.parseAndMapMessageByControlId([hl7FileStream])

        then:
        thrown(HL7FileMatcherException)
    }

    def "should throw HL7FileMatcherException when not able to parse the file as HL7 message"() {
        given:
        def inputStream = new ByteArrayInputStream("".bytes)
        def hl7FileStream = new HL7FileStream("badFile", inputStream)

        when:
        fileMatcher.parseAndMapMessageByControlId([hl7FileStream])

        then:
        thrown(HL7FileMatcherException)
    }

    def "should throw HL7FileMatcherException when unable to load a file to parse and map"() {
        def inputStream = Mock(ByteArrayInputStream)
        inputStream.readAllBytes() >> {
            throw new IOException("")
        }
        def hl7FileStream = new HL7FileStream("file1", inputStream)

        when:
        fileMatcher.parseAndMapMessageByControlId([hl7FileStream])

        then:
        thrown(HL7FileMatcherException)
    }
}
