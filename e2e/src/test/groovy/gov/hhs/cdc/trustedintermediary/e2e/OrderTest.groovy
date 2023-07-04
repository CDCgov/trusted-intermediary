package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class OrderTest extends Specification {

    def labOrderJsonFileString = Files.readString(Paths.get("src/test/resources/lab_order.json"))

    def "an order response is returned from the ETOR order endpoint"() {
        given:
        def expectedFhirResourceId  = "Bundle/969bcbb3-cd34-49be-ac4f-e1b8479b8219"
        def expectedPatientId  = "MRN7465737865"

        when:
        def responseBody = DemographicsClient.submitDemographics(labOrderJsonFileString)
        def parsedJsonBody = JsonParsing.parse(responseBody, Map.class)

        then:
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "bad response given for poorly formatted JSON"() {

        when:
        def responseBody = OrderClient.submitOrder(labOrderJsonFileString.substring(1))
        //removed beginning '{' to make this JSON invalid

        then:
        responseBody == "Server Error"
    }

    def "payload file check"() {

        when:
        def responseBody = OrderClient.submitOrder(labOrderJsonFileString)
        def sentPayload = SentPayloadReader.read()
        def parsedResponseBody = JsonParsing.parse(responseBody, Map.class)

        def parsedSentPayload = JsonParsing.parse(sentPayload, Map.class)

        then:

        parsedSentPayload.entry[0].resource.resourceType == "MessageHeader"
        parsedSentPayload.entry[3].resource.resourceType == "ServiceRequest"

        parsedSentPayload.entry[2].resource.resourceType == "Patient"
        parsedSentPayload.entry[2].resource.id == "infant-twin-1"

        parsedSentPayload.entry[2].resource.identifier[0].value == parsedResponseBody.patientId  //the second (index 1) identifier so happens to be the MRN
        parsedSentPayload.resourceType + "/" + parsedSentPayload.id == parsedResponseBody.fhirResourceId
    }

    def "a 401 comes from the ETOR order endpoint when unauthenticated"() {
        when:
        def response = DemographicsClient.submitDemographicsRaw(labOrderJsonFileString, false)

        then:
        response.getCode() == 401
    }
}
