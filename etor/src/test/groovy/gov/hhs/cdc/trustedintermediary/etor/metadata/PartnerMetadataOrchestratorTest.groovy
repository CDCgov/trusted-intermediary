package gov.hhs.cdc.trustedintermediary.etor.metadata

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.etor.orders.Order
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
        def mockOrder = Mock(Order)

        def partnerMetadataStorage = Mock(PartnerMetadataStorage)

        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, mockOrder)

        then:
        1 * partnerMetadataStorage.saveMetadata(_ as PartnerMetadata)
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

    def "updateMetadataForSentOrder test case when sentSubmissionId is null"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = null
        def partnerMetadataStorage = Mock(PartnerMetadataStorage)

        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        0 * partnerMetadataStorage.readMetadata(receivedSubmissionId)
    }

    def "updateMetadataForSentOrder test case when PartnerMetadata returns no data"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"

        def partnerMetadataStorage = Mock(PartnerMetadataStorage)
        partnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.empty()

        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
        thrown(NoSuchElementException)
    }

    def "getMetadata returns empty Optional when data is not found"() {
        given:
        String receivedSubmissionId = "receivedSubmissionId"
        def mockMetadata = Optional.empty()
        def partnerMetadataStorage = Mock(PartnerMetadataStorage)

        TestApplicationContext.register(PartnerMetadataStorage, partnerMetadataStorage)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        Optional<PartnerMetadata> result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        !result.isPresent()
        1 * partnerMetadataStorage.readMetadata(receivedSubmissionId) >> mockMetadata
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

    def "getReceiverName throws FormatterProcessingException for invalid JSON response"() {
        given:
        String invalidJson = "invalid JSON"

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().getReceiverName(invalidJson)

        then:
        thrown(FormatterProcessingException)
    }

    def "getReceiverName throws FormatterProcessingException for JSON without destinations"() {
        given:
        String jsonWithoutDestinations = "{\"someotherkey\": \"value\"}"

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutDestinations)

        then:
        thrown(FormatterProcessingException)
    }

    def "getReceiverName throws FormatterProcessingException for JSON with empty destinations"() {
        given:
        String jsonWithEmptyDestinations = "{\"destinations\": []}"

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithEmptyDestinations)

        then:
        thrown(FormatterProcessingException)
    }

    def "getReceiverName throws FormatterProcessingException for JSON without organization_id"() {
        given:
        String jsonWithoutOrgId = "{\"destinations\":[{\"service\":\"service\"}]}"

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutOrgId)

        then:
        thrown(FormatterProcessingException)
    }

    def "getReceiverName throws FormatterProcessingException for JSON without service"() {
        given:
        String jsonWithoutService = "{\"destinations\":[{\"organization_id\":\"org_id\"}]}"

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadataOrchestrator.getInstance().getReceiverName(jsonWithoutService)

        then:
        thrown(FormatterProcessingException)
    }
}
