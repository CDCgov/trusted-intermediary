package gov.hhs.cdc.trustedintermediary.etor.metadata

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference

import java.time.Instant
import spock.lang.Specification

class PartnerMetadataOrchestratorTest extends Specification {

    private def mockPartnerMetadataStorage
    private def mockClient
    private def mockFormatter

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        mockPartnerMetadataStorage = Mock(PartnerMetadataStorage)
        mockFormatter = Mock(Formatter)
        mockClient = Mock(RSEndpointClient)

        TestApplicationContext.register(PartnerMetadataOrchestrator, PartnerMetadataOrchestrator.getInstance())
        TestApplicationContext.register(PartnerMetadataStorage, mockPartnerMetadataStorage)
        TestApplicationContext.register(RSEndpointClient, mockClient)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "updateMetadataForReceivedOrder updates metadata successfully"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sender = "senderName"
        def timestamp = "2020-01-01T00:00:00.000Z"
        def hashCode = "123"
        def bearerToken = "token"
        def rsHistoryApiResponse = "{\"sender\": \"${sender}\", \"timestamp\": \"${timestamp}\"}"

        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [sender: sender, timestamp: timestamp]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, hashCode)

        then:
        1 * mockClient.getRsToken() >> bearerToken
        1 * mockClient.requestHistoryEndpoint(receivedSubmissionId, bearerToken) >> rsHistoryApiResponse
        1 * mockPartnerMetadataStorage.saveMetadata(new PartnerMetadata(receivedSubmissionId, sender, Instant.parse(timestamp), hashCode))
    }

    def "updateMetadataForSentOrder test case when sentSubmissionId is null"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = null

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        0 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId)
    }

    def "updateMetadataForSentOrder test case when PartnerMetadata returns no data"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"

        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.empty()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        0 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata)
    }

    def "getMetadata returns empty Optional when data is not found"() {
        given:
        String receivedSubmissionId = "receivedSubmissionId"
        def mockMetadata = Optional.empty()

        when:
        def result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        !result.isPresent()
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> mockMetadata
    }

    def "updateMetadataForReceivedOrder throws PartnerMetadataException on client error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"

        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> { throw new ReportStreamEndpointClientException("Client error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, "hash")

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedOrder throws PartnerMetadataException on formatter error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def rsHistoryApiResponse = "{\"sender\": \"responseName\", \"timestamp\": \"2020-01-01T00:00:00.000Z\"}"

        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> { throw new FormatterProcessingException("Formatter error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, "hash")

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedOrder throws PartnerMetadataException on formatter error due to unexpected response format"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def wrongFormatResponse = "{\"someotherkey\": \"value\"}"

        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> wrongFormatResponse
        mockFormatter.convertJsonToObject(wrongFormatResponse, _ as TypeReference) >> [someotherkey: "value"]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, "hash")

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
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sender", Instant.now(), "hash")
        def updatedPartnerMetadata = partnerMetadata.withSentSubmissionId(sentSubmissionId).withReceiver(receiver)

        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [destinations: [
                [organization_id: "org", service: "service"]
            ]]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        1 * mockClient.getRsToken() >> bearerToken
        1 * mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(updatedPartnerMetadata)
    }

    def "updateMetadataForSentOrder test case when sentSubmissionId is null"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = null

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        0 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId)
    }

    def "updateMetadataForSentOrder throws PartnerMetadataException on client error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, "sender", "receiver", Instant.now(), "hash")

        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> { throw new ReportStreamEndpointClientException("Client error", new Exception()) }

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
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, "sender", "receiver", Instant.now(), "hash")

        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> { throw new FormatterProcessingException("Formatter error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        thrown(PartnerMetadataException)
    }

    def "getMetadata retrieves metadata successfully"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def metadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", "receiver", Instant.now(), "hash")

        when:
        def result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == metadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(metadata)
    }

    def "getMetadata retrieves metadata successfully when receiver is present and sentSubmissionId is missing"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def metadata = new PartnerMetadata(receivedSubmissionId, null, "sender", "receiver", Instant.now(), "hash")

        when:
        def result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == metadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(metadata)
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
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, null, timestamp, hashCode)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, "org.service", timestamp, hashCode)

        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [destinations: [
                [organization_id: "org", service: "service"]
            ]]

        when:
        Optional<PartnerMetadata> result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == expectedMetadata
        2 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(missingReceiverMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(expectedMetadata)
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(expectedMetadata)
    }

    def "getReceiverName returns correct receiver name from valid JSON response"() {
        given:
        def validJson = "{\"destinations\": [{\"organization_id\": \"org_id\", \"service\": \"service_name\"}]}"

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def receiverName = PartnerMetadataOrchestrator.getInstance().getReceiverName(validJson)

        then:
        receiverName == "org_id.service_name"
    }

    def "getReceiverName throws FormatterProcessingException for unexpected format response"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def invalidJson = "invalid JSON"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(invalidJson)

        then:
        thrown(FormatterProcessingException)

        when:
        def emptyJson = "{}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(emptyJson)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithoutDestinations = "{\"someotherkey\": \"value\"}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutDestinations)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithEmptyDestinations = "{\"destinations\": []}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithEmptyDestinations)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithoutOrgId = "{\"destinations\":[{\"service\":\"service\"}]}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutOrgId)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithoutService = "{\"destinations\":[{\"organization_id\":\"org_id\"}]}"
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutService)

        then:
        thrown(FormatterProcessingException)
    }
}
