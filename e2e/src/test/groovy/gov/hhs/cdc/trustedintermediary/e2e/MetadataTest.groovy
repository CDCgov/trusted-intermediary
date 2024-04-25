package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class MetadataTest extends Specification {

    def metadataClient = new MetadataClient()
    def orderClient = new EndpointClient("/v1/etor/orders")
    def resultClient = new EndpointClient("/v1/etor/results")

    def placerOrderNumberFhirPath = "Bundle.entry.resource.ofType(ServiceRequest).identifier.where(type.coding.code = 'PLAC').value"
    def sendingFacilityIdFhirPath = "Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and extension.value = 'HD.2,HD.3').value"
    def receivingFacilityIdFhirPath = "Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and extension.value = 'HD.2,HD.3').value"

    def setup() {
        SentPayloadReader.delete()
    }

    def "a metadata response is returned from the ETOR metadata endpoint"() {
        given:
        def orderFhirString = Files.readString(Path.of("../examples/Test/e2e/orders/002_ORM_O01.fhir"))
        def expectedStatusCode = 200
        def inboundSubmissionId = UUID.randomUUID().toString()

        when:
        def orderResponse = orderClient.submit(orderFhirString, inboundSubmissionId, true)

        then:
        orderResponse.getCode() == expectedStatusCode

        when:
        def inboundMetadataResponse = metadataClient.get(inboundSubmissionId, true)
        def inboundParsedJsonBody = JsonParser.parseContent(inboundMetadataResponse)
        def outboundSubmissionId = inboundParsedJsonBody.issue.find {it.details.text == 'outbound submission id' }.diagnostics
        def outboundMetadataResponse = metadataClient.get(outboundSubmissionId, true)
        def outboundParsedJsonBody = JsonParser.parseContent(outboundMetadataResponse)

        then:
        inboundMetadataResponse.getCode() == expectedStatusCode
        outboundMetadataResponse.getCode() == expectedStatusCode
        inboundParsedJsonBody.get("id") == inboundSubmissionId
        outboundParsedJsonBody.get("id") == outboundSubmissionId

        [
            "linked messages",
            "sender universal id",
            "receiver universal id",
            "ingestion",
            "payload hash",
            "delivery status",
            "status message",
            "message type",
            "outbound submission id",
            "inbound submission id"
        ].each { String metadataKey ->
            def issue = (inboundParsedJsonBody.issue as List).find( {issue -> issue.details.text == metadataKey })
            assert issue != null
            assert issue.diagnostics != null
            assert !issue.diagnostics.isEmpty()
        }
    }

    def "a metadata response is returned from the ETOR metadata endpoint for results"() {
        given:
        def resultFhirString = Files.readString(Path.of("../examples/Test/e2e/results/001_ORU_R01.fhir"))
        def expectedStatusCode = 200
        def inboundSubmissionId = UUID.randomUUID().toString()

        when:
        def resultResponse = resultClient.submit(resultFhirString, inboundSubmissionId, true)

        then:
        resultResponse.getCode() == expectedStatusCode

        when:
        def inboundMetadataResponse = metadataClient.get(inboundSubmissionId, true)
        def inboundParsedJsonBody = JsonParser.parseContent(inboundMetadataResponse)
        def outboundSubmissionId = inboundParsedJsonBody.issue.find {it.details.text == 'outbound submission id' }.diagnostics
        def outboundMetadataResponse = metadataClient.get(outboundSubmissionId, true)
        def outboundParsedJsonBody = JsonParser.parseContent(outboundMetadataResponse)

        then:
        inboundMetadataResponse.getCode() == expectedStatusCode
        outboundMetadataResponse.getCode() == expectedStatusCode
        inboundParsedJsonBody.get("id") == inboundSubmissionId
        outboundParsedJsonBody.get("id") == outboundSubmissionId

        [
            "linked messages",
            "sender universal id",
            "receiver universal id",
            "ingestion",
            "payload hash",
            "delivery status",
            "status message",
            "message type",
            "outbound submission id",
            "inbound submission id"
        ].each { String metadataKey ->
            def issue = (inboundParsedJsonBody.issue as List).find( {issue -> issue.details.text == metadataKey })
            assert issue != null
            assert issue.diagnostics != null
            assert !issue.diagnostics.isEmpty()
        }
    }

    def "a 404 is returned when there is no metadata for a given ID"() {
        when:
        def metadataResponse = metadataClient.get(UUID.randomUUID().toString(), true)
        def parsedJsonBody = JsonParser.parseContent(metadataResponse)

        then:
        metadataResponse.getCode() == 404
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "metadata endpoint fails when called un an unauthenticated manner"() {
        when:
        def metadataResponse = metadataClient.get("DogCow", false)
        def parsedJsonBody = JsonParser.parseContent(metadataResponse)

        then:
        metadataResponse.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "linked id for the corresponding message is included when retrieving linked metadata"() {
        given:
        def orderSubmissionId = UUID.randomUUID().toString()
        def orderFhirString = Files.readString(Path.of("../examples/Test/e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir"))
        def orderResolvedFhir = HapiParser.parse(orderFhirString)
        def orderPlacerOrderNumber = HapiParser.getStringFromFhirPath(orderResolvedFhir, placerOrderNumberFhirPath)
        def orderSendingFacilityId = HapiParser.getStringFromFhirPath(orderResolvedFhir, sendingFacilityIdFhirPath)
        def orderResponse = orderClient.submit(orderFhirString, orderSubmissionId, true)

        def resultSubmissionId = UUID.randomUUID().toString()
        def resultFhirString = Files.readString(Path.of("../examples/Test/e2e/results/002_2_ORU_R01_short_linked_to_003_ORM_O01_short.fhir"))
        def resultResolvedFhir = HapiParser.parse(resultFhirString)
        def resultPlacerOrderNumber = HapiParser.getStringFromFhirPath(resultResolvedFhir, placerOrderNumberFhirPath)
        def resultReceivingFacilityId = HapiParser.getStringFromFhirPath(resultResolvedFhir, receivingFacilityIdFhirPath)
        def resultResponse = orderClient.submit(resultFhirString, resultSubmissionId, true)

        expect:
        orderPlacerOrderNumber == resultPlacerOrderNumber
        orderSendingFacilityId == resultReceivingFacilityId
        orderResponse.getCode() == 200
        resultResponse.getCode() == 200

        when:
        def metadataForOrderResponse = metadataClient.get(orderSubmissionId, true)
        def metadataForOrderParsedJson = JsonParser.parseContent(metadataForOrderResponse)
        def linkedIdsForOrder = metadataForOrderParsedJson.issue.find { it.details.text == 'linked messages' }.diagnostics

        then:
        linkedIdsForOrder.contains(resultSubmissionId)

        when:
        def metadataForResultResponse = metadataClient.get(resultSubmissionId, true)
        def metadataForResultParsedJson = JsonParser.parseContent(metadataForResultResponse)
        def linkedIdsForResult = metadataForResultParsedJson.issue.find { it.details.text == 'linked messages' }.diagnostics

        then:
        linkedIdsForResult.contains(orderSubmissionId)
    }

    def "a metadata order is not linked when the corresponding linking fields do not match"() {
        given:
        def orderSubmissionId = UUID.randomUUID().toString()
        def orderFhirString = Files.readString(Path.of("../examples/Test/e2e/orders/003_2_ORM_O01_short_not_linked_to_002_ORU_R01_short.fhir"))
        def orderResolvedFhir = HapiParser.parse(orderFhirString)
        def orderPlacerOrderNumber = HapiParser.getStringFromFhirPath(orderResolvedFhir, placerOrderNumberFhirPath)
        def orderSendingFacilityId = HapiParser.getStringFromFhirPath(orderResolvedFhir, sendingFacilityIdFhirPath)
        def orderResponse = orderClient.submit(orderFhirString, orderSubmissionId, true)

        def resultSubmissionId = UUID.randomUUID().toString()
        def resultFhirString = Files.readString(Path.of("../examples/Test/e2e/results/002_2_ORU_R01_short_linked_to_003_ORM_O01_short.fhir"))
        def resultResolvedFhir = HapiParser.parse(resultFhirString)
        def resultPlacerOrderNumber = HapiParser.getStringFromFhirPath(resultResolvedFhir, placerOrderNumberFhirPath)
        def resultReceivingFacilityId = HapiParser.getStringFromFhirPath(resultResolvedFhir, receivingFacilityIdFhirPath)
        def resultResponse = resultClient.submit(resultFhirString, resultSubmissionId, true)

        expect:
        orderPlacerOrderNumber != resultPlacerOrderNumber
        orderSendingFacilityId != resultReceivingFacilityId
        orderResponse.getCode() == 200
        resultResponse.getCode() == 200

        when:
        def metadataResponse = metadataClient.get(orderSubmissionId, true)
        def metadataParsedJson = JsonParser.parseContent(metadataResponse)
        def linkedIds = metadataParsedJson.issue.find { it.details.text == 'linked messages' }.diagnostics

        then:
        !linkedIds.contains(resultSubmissionId)
    }
}
