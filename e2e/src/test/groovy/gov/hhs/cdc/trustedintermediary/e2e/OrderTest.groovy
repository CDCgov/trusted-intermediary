package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class OrderTest extends Specification {

    def orderClient = new EndpointClient("/v1/etor/orders")
    def labOrderJsonFileString = Files.readString(Path.of("../examples/MN/001_MN_Order_NBS.fhir"))
    def submissionId = "submissionId"

    def setup() {
        SentPayloadReader.delete()
    }

    def "an order response is returned from the ETOR order endpoint"() {
        given:
        def expectedFhirResourceId  = "Bundle/1696524903034430000.eb38702e-23df-4650-9e4c-c7d4b3b6b92b"
        def expectedPatientId  = "11102779"

        when:
        def response = orderClient.submit(labOrderJsonFileString, submissionId, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 200
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "check that contact info is added to order before sending to report stream"() {
        when:
        orderClient.submit(labOrderJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParsing.parse(sentPayload)

        then:
        parsedSentPayload.entry[24].resource.contact.name.text.contains("SADIE S SMITH")
    }

    def "check that the rest of the message is unchanged except the parts we changed"() {
        when:
        orderClient.submit(labOrderJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParsing.parse(sentPayload)
        def parsedLabOrderJsonFile = JsonParsing.parse(labOrderJsonFileString)

        then:
        //test that everything else is the same except the MessageHeader's event and Patient contact
        parsedSentPayload.entry[0].resource.remove("eventCoding")
        parsedLabOrderJsonFile.entry[0].resource.remove("eventCoding")
        parsedSentPayload.entry[24].resource.remove("contact")

        parsedSentPayload == parsedLabOrderJsonFile
    }

    def "check that message type is converted to OML_O21"() {
        when:
        orderClient.submit(labOrderJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParsing.parse(sentPayload)


        then:
        //test that the MessageHeader's event is now an OML_O21
        parsedSentPayload.entry[0].resource.resourceType == "MessageHeader"
        parsedSentPayload.entry[0].resource.eventCoding.code == "O21"
        parsedSentPayload.entry[0].resource.eventCoding.display.contains("OML")
    }

    def "return a 400 response when request has unexpected format"() {
        given:
        def invalidJsonRequest = labOrderJsonFileString.substring(1)

        when:
        def response = orderClient.submit(invalidJsonRequest, submissionId, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 400
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "return a 401 response when making an unauthenticated request"() {
        when:
        def response = orderClient.submit(labOrderJsonFileString, submissionId, false)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
