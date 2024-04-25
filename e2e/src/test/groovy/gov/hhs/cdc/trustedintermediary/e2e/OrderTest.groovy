package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class OrderTest extends Specification {

    def orderClient = new EndpointClient("/v1/etor/orders")
    def labOrderJsonFileString = Files.readString(Path.of("../examples/Test/e2e/orders/002_ORM_O01.fhir"))
    def submissionId = "submissionId"

    def setup() {
        SentPayloadReader.delete()
    }

    def "an order response is returned from the ETOR order endpoint"() {
        given:
        def expectedFhirResourceId  = "Bundle/1713991685806650392.f865cc8e-d438-4d5f-9147-05930f25a997"
        def expectedPatientId  = "11102779"

        when:
        def response = orderClient.submit(labOrderJsonFileString, submissionId, true)
        def parsedJsonBody = JsonParser.parseContent(response)

        then:
        response.getCode() == 200
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "check that contact info is added to order before sending to report stream"() {
        when:
        orderClient.submit(labOrderJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParser.parse(sentPayload)

        then:
        parsedSentPayload.entry[3].resource.contact[0].name.family.contains("SMITH")
    }

    def "check that ETOR processing code is added to the order before sending to report stream"() {
        given:
        var loginFirst = true
        def expectedSystem = "http://localcodes.org/ETOR"
        def expectedCode = "ETOR"
        def expectedDisplay = "Processed by ETOR"

        when:
        orderClient.submit(labOrderJsonFileString, submissionId, loginFirst)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayLoad = JsonParser.parse(sentPayload)
        def actualSystem = parsedSentPayLoad.entry[0].resource.meta.tag[1].system
        def actualCode = parsedSentPayLoad.entry[0].resource.meta.tag[1].code
        def actualDisplay = parsedSentPayLoad.entry[0].resource.meta.tag[1].display

        then:
        actualSystem == expectedSystem
        actualCode == expectedCode
        actualDisplay == expectedDisplay
    }

    def "check that the rest of the message is unchanged except the parts we changed"() {
        when:
        orderClient.submit(labOrderJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParser.parse(sentPayload)
        def parsedLabOrderJsonFile = JsonParser.parse(labOrderJsonFileString)

        then:
        //test that everything else is the same except the MessageHeader's event, Patient contact, and etor processing tag
        parsedSentPayload.entry[0].resource.remove("eventCoding")
        parsedLabOrderJsonFile.entry[0].resource.remove("eventCoding")
        parsedSentPayload.entry[3].resource.remove("contact")
        parsedSentPayload.entry[0].resource.meta.tag.remove(1)

        parsedSentPayload == parsedLabOrderJsonFile
    }

    def "check that message type is converted to OML_O21"() {
        when:
        orderClient.submit(labOrderJsonFileString, submissionId, true)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParser.parse(sentPayload)


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
        def parsedJsonBody = JsonParser.parseContent(response)

        then:
        response.getCode() == 400
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "return a 401 response when making an unauthenticated request"() {
        when:
        def response = orderClient.submit(labOrderJsonFileString, submissionId, false)
        def parsedJsonBody = JsonParser.parseContent(response)

        then:
        response.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
