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
        def submissionId = UUID.randomUUID().toString()
        def orderClient = new EndpointClient("/v1/etor/orders")
        def labOrderJsonFileString = Files.readString(Path.of("../examples/fhir/MN NBS FHIR Order Message.json"))

        when:
        def orderResponse = orderClient.submit(labOrderJsonFileString, submissionId, true)

        then:
        orderResponse.getCode() == 200

        when:
        def metadataResponse = metadataClient.get(submissionId, true)
        def parsedJsonBody = JsonParsing.parseContent(metadataResponse)

        then:
        metadataResponse.getCode() == 200
        parsedJsonBody.uniqueId == submissionId
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
