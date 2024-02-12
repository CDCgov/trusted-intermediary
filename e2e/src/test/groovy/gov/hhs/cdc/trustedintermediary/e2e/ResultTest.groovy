package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ResultTest extends Specification {
    def resultClient = new EndpointClient("/v1/etor/results")

    def labResultJsonFileString = Files.readString(Path.of("../examples/MN/004_MN_ORU_R01_NBS_1_translation_from_initial_hl7_ingestion.fhir"))

    def submissionId = "submissionId"

    def setup() {
        SentPayloadReader.delete()
    }

    def "a result response is returned from the ETOR order endpoint"() {
        given:
        def expectedFhirResourceId  = "Bundle/1705511861639940150.e2c69100-24af-4bbd-86bf-2f29be816edf"

        when:
        def response = resultClient.submit(labResultJsonFileString, submissionId, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 200
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
    }

    // Will progress on these tests when sending data to RS
    def "check that the rest of the message is unchanged except the parts we changed"() {
        when:
        resultClient.submit(labResultJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParsing.parse(sentPayload)
        def parsedLabResultJsonFile = JsonParsing.parse(labResultJsonFileString)

        then:
        parsedSentPayload.entry[0].resource.meta.tag.remove(1)
        parsedSentPayload == parsedLabResultJsonFile
    }

    def "check that message type is converted to ORU_R01"() {
        when:
        resultClient.submit(labResultJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParsing.parse(sentPayload)

        then:
        //test that the MessageHeader's event is now an OML_O21
        parsedSentPayload.entry[0].resource.resourceType == "MessageHeader"
        parsedSentPayload.entry[0].resource.eventCoding.code == "R01"
        parsedSentPayload.entry[0].resource.eventCoding.display.contains("ORU")
    }

    def "return a 400 response when request has unexpected format"() {
        given:
        def invalidJsonRequest = labResultJsonFileString.substring(1)

        when:
        def response = resultClient.submit(invalidJsonRequest, submissionId, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 400
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "return a 401 response when making an unauthenticated request"() {
        when:
        def response = resultClient.submit(labResultJsonFileString, submissionId, false)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
