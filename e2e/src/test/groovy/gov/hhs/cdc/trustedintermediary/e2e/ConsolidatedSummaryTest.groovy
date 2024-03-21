package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ConsolidatedSummaryTest extends Specification {

    def ConsolidatedSummaryClient = new ConsolidatedSummaryClient()
    def setup() {
        SentPayloadReader.delete()
    }

    def "a consolidated summary is returned from the ETOR consolidated summary endpoint"() {
        given:
        def expectedStatusCode = 200
        def inboundSubmissionId = UUID.randomUUID().toString()

        def orderClient = new EndpointClient("/v1/etor/orders")
        def labOrderJsonFileString = Files.readString(Path.of("../examples/Test/Orders/002_ORM_O01.fhir"))
        def senderName = "flexion.simulated-hospital"
        when:
        def orderResponse = orderClient.submit(labOrderJsonFileString, inboundSubmissionId, true)
        then:
        orderResponse.getCode() == expectedStatusCode

        when:
        def senderNameResponse = ConsolidatedSummaryClient.get(senderName, true)
        def jsonBody = JsonParsing.parseContent(senderNameResponse)
        then:
        jsonBody.get((jsonBody.keySet().toArray())[0]).stale != null
        jsonBody.get((jsonBody.keySet().toArray())[0]).failureReason == null
        jsonBody.get((jsonBody.keySet().toArray())[0]).status != null
    }

    def "consolidated endpoint fails when called while not authenticated"() {
        when:
        def consolidatedResponse = ConsolidatedSummaryClient.get("test", false)
        def parsedJsonBody = JsonParsing.parseContent(consolidatedResponse)

        then:
        consolidatedResponse.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
