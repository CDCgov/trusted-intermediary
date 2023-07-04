package gov.hhs.cdc.trustedintermediary.e2e

import org.apache.hc.core5.http.io.entity.EntityUtils
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
        def response = orderClient.submit(labOrderJsonFileString, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 200
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "bad response given for poorly formatted JSON"() {
        given:
        def invalidJsonRequest = labOrderJsonFileString.substring(1)

        when:
        def response = orderClient.submit(invalidJsonRequest, true)
        def responseBody = EntityUtils.toString(response.getEntity())

        then:
        response.getCode() == 500
        responseBody == "Server Error"
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

    def "a 401 comes from the ETOR order endpoint when unauthenticated"() {
        when:
        def response = orderClient.submit(labOrderJsonFileString, false)

        then:
        response.getCode() == 401
    }
}
