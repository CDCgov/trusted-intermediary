package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class MetadataTest extends Specification {

    def metadataClient = new MetadataClient()

    def setup() {
        SentPayloadReader.delete()
    }

    def "a metadata response is returned from the ETOR metadata endpoint"() {
        given:
        def expectedStatusCode = 200
        def submissionId = UUID.randomUUID().toString()
        def orderClient = new EndpointClient("/v1/etor/orders")
        def labOrderJsonFileString = Files.readString(Path.of("../examples/MN/001_MN_Order_NBS.fhir"))

        when:
        def orderResponse = orderClient.submit(labOrderJsonFileString, submissionId, true)

        then:
        orderResponse.getCode() == expectedStatusCode

        when:
        def metadataResponse = metadataClient.get(submissionId, true)
        def parsedJsonBody = JsonParsing.parseContent(metadataResponse)

        then:
        metadataResponse.getCode() == expectedStatusCode
        parsedJsonBody.get("id") == submissionId

        [
            "sender name",
            "receiver name",
            "order ingestion",
            "payload hash",
            "delivery status",
            "status message"
        ].each { String metadataKey ->
            def issue = (parsedJsonBody.issue as List).find( {issue -> issue.details.text == metadataKey })
            assert issue != null
            assert issue.diagnostics != null
            assert !issue.diagnostics.isEmpty()
        }
    }

    def "a 404 is returned when there is no metadata for a given ID"() {
        when:
        def metadataResponse = metadataClient.get(UUID.randomUUID().toString(), true)
        def parsedJsonBody = JsonParsing.parseContent(metadataResponse)

        then:
        metadataResponse.getCode() == 404
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "metadata endpoint fails when called un an unauthenticated manner"() {
        when:
        def metadataResponse = metadataClient.get("DogCow", false)
        def parsedJsonBody = JsonParsing.parseContent(metadataResponse)

        then:
        metadataResponse.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
