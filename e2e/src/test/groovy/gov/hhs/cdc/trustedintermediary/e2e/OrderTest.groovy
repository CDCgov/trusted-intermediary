package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class OrderTest extends Specification {

    def orderClient = new EndpointClient("/v1/etor/orders")
    def labOrderJsonFileString = Files.readString(Paths.get("src/test/resources/lab_order.json"))

    def "an order response is returned from the ETOR order endpoint"() {
        given:
        def expectedFhirResourceId  = "Bundle/969bcbb3-cd34-49be-ac4f-e1b8479b8219"
        def expectedPatientId  = "MRN7465737865"

        when:
        def responseBody = orderClient.submit(labOrderJsonFileString)
        def parsedJsonBody = JsonParsing.parse(responseBody, Map.class)

        then:
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "bad response given for poorly formatted JSON"() {

        when:
        def responseBody = orderClient.submit(labOrderJsonFileString.substring(1))
        //removed beginning '{' to make this JSON invalid

        then:
        responseBody == "Server Error"
    }

    def "payload file check"() {

        when:
        def sentPayload = SentPayloadReader.read()
        def parsedLabOrderJson = JsonParsing.parse(labOrderJsonFileString, Map.class)
        def parsedSentPayload = JsonParsing.parse(sentPayload, Map.class)

        then:

        parsedSentPayload == parsedLabOrderJson
    }

    def "a 401 comes from the ETOR order endpoint when unauthenticated"() {
        when:
        def response = orderClient.submitRaw(labOrderJsonFileString, false)

        then:
        response.getCode() == 401
    }
}
