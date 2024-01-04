package gov.hhs.cdc.trustedintermediary.etor.metadata

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.Order
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference

import java.time.Instant
import spock.lang.Specification

class PartnerMetadataOrchestratorTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PartnerMetadataOrchestrator, PartnerMetadataOrchestrator.getInstance())
    }

    def "updateMetadataForReceivedOrder updates metadata successfully"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "senderName"
        def timestamp = "2020-01-01T00:00:00.000Z"
        def hashCode = 123
        def bearerToken = "token"
        def rsHistoryApiResponse = "{\"sender\": \"${sender}\", \"timestamp\": \"${timestamp}\"}"

        def mockOrder = Mock(Order)
        mockOrder.hashCode() >> hashCode

        def partnerMetadataStorage = Mock(PartnerMetadataStorage)
        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)

        def mockClient = Mock(RSEndpointClient)
        TestApplicationContext.register(RSEndpointClient, mockClient)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [sender: sender, timestamp: timestamp]
        TestApplicationContext.register(Formatter, mockFormatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, mockOrder)

        then:
        1 * mockClient.getRsToken() >> bearerToken
        1 * mockClient.requestHistoryEndpoint(receivedSubmissionId, bearerToken) >> rsHistoryApiResponse
        1 * partnerMetadataStorage.saveMetadata(new PartnerMetadata(receivedSubmissionId, sender, Instant.parse(timestamp), hashCode.toString()))
    }

    def "updateMetadataForReceivedOrder throws PartnerMetadataException on client error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"

        def mockClient = Mock(RSEndpointClient)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> { throw new ReportStreamEndpointClientException("Client error", new Exception()) }
        TestApplicationContext.register(RSEndpointClient, mockClient)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, Mock(Order))

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedOrder throws PartnerMetadataException on formatter error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def rsHistoryApiResponse = "{\"sender\": \"responseName\", \"timestamp\": \"2020-01-01T00:00:00.000Z\"}"

        def mockClient = Mock(RSEndpointClient)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> rsHistoryApiResponse
        TestApplicationContext.register(RSEndpointClient, mockClient)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> { throw new FormatterProcessingException("Formatter error", new Exception()) }
        TestApplicationContext.register(Formatter, mockFormatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, Mock(Order))

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedOrder throws PartnerMetadataException on formatter error due to unexpected response format"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def wrongFormatResponse = "{\"someotherkey\": \"value\"}"

        def mockClient = Mock(RSEndpointClient)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> wrongFormatResponse
        TestApplicationContext.register(RSEndpointClient, mockClient)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(wrongFormatResponse, _ as TypeReference) >> [someotherkey: "value"]
        TestApplicationContext.register(Formatter, mockFormatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, Mock(Order))

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForSentOrder updates metadata successfully"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def bearerToken = "token"
        def receiver = "org.service"
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"

        def partnerMetadataStorage = Mock(PartnerMetadataStorage)
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sender", Instant.now(), "hash")
        def updatedPartnerMetadata = partnerMetadata.withSentSubmissionId(sentSubmissionId).withReceiver(receiver)
        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)

        def mockClient = Mock(RSEndpointClient)
        TestApplicationContext.register(RSEndpointClient, mockClient)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [destinations: [
                [organization_id: "org", service: "service"]
            ]]
        TestApplicationContext.register(Formatter, mockFormatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        1 * mockClient.getRsToken() >> bearerToken
        1 * mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        1 * partnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        1 * partnerMetadataStorage.saveMetadata(updatedPartnerMetadata)
    }

    def "updateMetadataForSentOrder throws PartnerMetadataException on client error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"

        def partnerMetadataStorage = Mock(PartnerMetadataStorage)
        PartnerMetadata partnerMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, "sender", "receiver", Instant.now(), "hash")
        partnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)

        def mockClient = Mock(RSEndpointClient)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> { throw new ReportStreamEndpointClientException("Client error", new Exception()) }
        TestApplicationContext.register(RSEndpointClient, mockClient)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForSentOrder throws PartnerMetadataException on formatter error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"

        def partnerMetadataStorage = Mock(PartnerMetadataStorage)
        PartnerMetadata partnerMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, "sender", "receiver", Instant.now(), "hash")
        partnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)

        def mockClient = Mock(RSEndpointClient)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> rsHistoryApiResponse
        TestApplicationContext.register(RSEndpointClient, mockClient)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> { throw new FormatterProcessingException("Formatter error", new Exception()) }
        TestApplicationContext.register(Formatter, mockFormatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        thrown(PartnerMetadataException)
    }

    def "getMetadata retrieves metadata successfully"() {
        given:
        String receivedSubmissionId = "receivedSubmissionId"
        PartnerMetadata metadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", "receiver", Instant.now(), "hash")

        def partnerMetadataStorage = Mock(PartnerMetadataStorage)
        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        Optional<PartnerMetadata> result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == metadata
        1 * partnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(metadata)
    }

    def "getMetadata gets receiver if missing from metadata"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "senderName"
        def timestamp = Instant.now()
        def hashCode = "123"
        def bearerToken = "token"
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"

        PartnerMetadata missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, null, timestamp, hashCode)
        PartnerMetadata expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, "org.service", timestamp, hashCode)

        def partnerMetadataStorage = Mock(PartnerMetadataStorage)
        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)

        def mockClient = Mock(RSEndpointClient)
        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        TestApplicationContext.register(RSEndpointClient, mockClient)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [destinations: [
                [organization_id: "org", service: "service"]
            ]]
        TestApplicationContext.register(Formatter, mockFormatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        Optional<PartnerMetadata> result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == expectedMetadata
        2 * partnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(missingReceiverMetadata)
        1 * partnerMetadataStorage.saveMetadata(expectedMetadata)
        1 * partnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(expectedMetadata)
    }

    def "getReceiverName returns correct receiver name from valid JSON response"() {
        given:
        String validJson = "{\"destinations\": [{\"organization_id\": \"org_id\", \"service\": \"service_name\"}]}"

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        String receiverName = PartnerMetadataOrchestrator.getInstance().getReceiverName(validJson)

        then:
        receiverName == "org_id.service_name"
    }

    def "getReceiverName throws FormatterProcessingException for unexpected format response"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        String invalidJson = "invalid JSON"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(invalidJson)

        then:
        thrown(FormatterProcessingException)

        when:
        String emptyJson = "{}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(emptyJson)

        then:
        thrown(FormatterProcessingException)

        when:
        String jsonWithoutDestinations = "{\"someotherkey\": \"value\"}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutDestinations)

        then:
        thrown(FormatterProcessingException)

        when:
        String jsonWithEmptyDestinations = "{\"destinations\": []}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithEmptyDestinations)

        then:
        thrown(FormatterProcessingException)

        when:
        String jsonWithoutOrgId = "{\"destinations\":[{\"service\":\"service\"}]}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutOrgId)

        then:
        thrown(FormatterProcessingException)

        when:
        String jsonWithoutService = "{\"destinations\":[{\"organization_id\":\"org_id\"}]}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutService)

        then:
        thrown(FormatterProcessingException)
    }
}
