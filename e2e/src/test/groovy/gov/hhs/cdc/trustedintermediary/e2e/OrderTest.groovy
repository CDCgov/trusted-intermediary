package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class OrderTest extends Specification {

    def orderClient = new EndpointClient("/v1/etor/orders")
    def labOrderJsonFileString = Files.readString(Path.of("../examples/fhir/MN NBS FHIR Order Message.json"))

    def "an order response is returned from the ETOR order endpoint"() {
        given:
        def expectedFhirResourceId  = "Bundle/b4efef3a-749c-457d-956b-568e22768bf3"
        def expectedPatientId  = "11102779"

        when:
        def response = orderClient.submit(labOrderJsonFileString, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 200
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "payload file check"() {
        when:
        def response = orderClient.submit(labOrderJsonFileString, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParsing.parse(sentPayload)
        def parsedLabOrderJsonFile = JsonParsing.parse(labOrderJsonFileString)

        then:
        response.getCode() == 200
        parsedSentPayload == parsedLabOrderJsonFile
    }

    def "return a 400 response when request has unexpected format"() {
        given:
        def invalidJsonRequest = labOrderJsonFileString.substring(1)

        when:
        def response = orderClient.submit(invalidJsonRequest, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 400
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "return a 401 response when making an unauthenticated request"() {
        when:
        def response = orderClient.submit(labOrderJsonFileString, false)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
