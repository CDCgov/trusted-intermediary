package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ResultTest extends Specification {
    def resultClient = new EndpointClient("/v1/etor/results")

    def labResultJsonFileString = Files.readString(Path.of("../examples/MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir"))

    def submissionId = "submissionId"

    def setup() {
        SentPayloadReader.delete()
    }

    def "a result response is returned from the ETOR order endpoint"() {
        given:
        def expectedJsonBody = JsonParser.parse(labResultJsonFileString)

        when:
        def response = resultClient.submit(labResultJsonFileString, submissionId, true)
        def parsedJsonBody = JsonParser.parseContent(response)

        then:
        response.getCode() == 200
        parsedJsonBody.fhirResourceId.contains(expectedJsonBody.id)
    }

    def "check that the rest of the message is unchanged except the parts we changed"() {
        when:
        resultClient.submit(labResultJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParser.parse(sentPayload)
        def parsedLabResultJsonFile = JsonParser.parse(labResultJsonFileString)

        then:
        parsedSentPayload.entry[0].resource.meta.tag.remove(1) // Remove ETOR meta tagging from tests
        parsedSentPayload == parsedLabResultJsonFile
    }

    def "check that message type is converted to ORU_R01"() {
        when:
        resultClient.submit(labResultJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParser.parse(sentPayload)

        then:
        //test that the MessageHeader's event is now an ORU_R01
        parsedSentPayload.entry[0].resource.resourceType == "MessageHeader"
        parsedSentPayload.entry[0].resource.eventCoding.code == "R01"
        parsedSentPayload.entry[0].resource.eventCoding.display.contains("ORU")
        def etorHeader = parsedSentPayload.entry[0].resource.meta.tag.get(1)
        etorHeader.system == "http://localcodes.org/ETOR"
        etorHeader.code == "ETOR"
        etorHeader.display == "Processed by ETOR"
    }

    def "return a 400 response when request has unexpected format"() {
        given:
        def invalidJsonRequest = labResultJsonFileString.substring(1)

        when:
        def response = resultClient.submit(invalidJsonRequest, submissionId, true)
        def parsedJsonBody = JsonParser.parseContent(response)

        then:
        response.getCode() == 400
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "return a 401 response when making an unauthenticated request"() {
        when:
        def response = resultClient.submit(labResultJsonFileString, submissionId, false)
        def parsedJsonBody = JsonParser.parseContent(response)

        then:
        response.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
