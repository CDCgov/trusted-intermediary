package gov.hhs.cdc.trustedintermediary.etor.metadata.partner

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrderConverter
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
        TestApplicationContext.register(OrderConverter, HapiOrderConverter.getInstance())
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
        def deliveryStatus = PartnerMetadataStatus.PENDING

        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [sender: sender, timestamp: timestamp]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, hashCode)

        then:
        1 * mockClient.getRsToken() >> bearerToken
        1 * mockClient.requestHistoryEndpoint(receivedSubmissionId, bearerToken) >> rsHistoryApiResponse
        1 * mockPartnerMetadataStorage.saveMetadata(new PartnerMetadata(receivedSubmissionId, sender, Instant.parse(timestamp), hashCode, deliveryStatus))
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
        1 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata) >> { PartnerMetadata metadata ->
            assert metadata.receivedSubmissionId() == receivedSubmissionId
        }
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
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sender", Instant.now(), "hash", PartnerMetadataStatus.PENDING)
        def updatedPartnerMetadata = partnerMetadata.withSentSubmissionId(sentSubmissionId)

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)

        then:
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

    def "getMetadata throws PartnerMetadataException on client error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, "sender", null, Instant.now(), "hash", PartnerMetadataStatus.PENDING)

        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> { throw new ReportStreamEndpointClientException("Client error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        thrown(PartnerMetadataException)
    }

    def "getMetadata throws PartnerMetadataException on formatter error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, "sender", null, Instant.now(), "hash", PartnerMetadataStatus.PENDING)

        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> { throw new FormatterProcessingException("Formatter error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        thrown(PartnerMetadataException)
    }

    def "getMetadata retrieves metadata successfully with the sender already filled"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def metadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", "receiver", Instant.now(), "hash", PartnerMetadataStatus.DELIVERED)

        when:
        def result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == metadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(metadata)
        0 * mockClient.requestHistoryEndpoint(_, _)
    }

    def "getMetadata retrieves metadata successfully when receiver is present and sentSubmissionId is missing"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def metadata = new PartnerMetadata(receivedSubmissionId, null, "sender", "receiver", Instant.now(), "hash", PartnerMetadataStatus.DELIVERED)

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
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, null, timestamp, hashCode, PartnerMetadataStatus.DELIVERED)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, "org.service", timestamp, hashCode, PartnerMetadataStatus.DELIVERED)

        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [
            overallStatus: "Delivered",
            destinations: [
                [organization_id: "org", service: "service"]
            ]]

        when:
        Optional<PartnerMetadata> result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == expectedMetadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(missingReceiverMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(expectedMetadata)
    }

    def "getMetadata gets status if still pending in metadata"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "senderName"
        def receiver = "org.service"
        def timestamp = Instant.now()
        def hashCode = "123"
        def bearerToken = "token"
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, hashCode, PartnerMetadataStatus.PENDING)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, hashCode, PartnerMetadataStatus.FAILED)

        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [
            overallStatus: "Not Delivering",
            destinations: [
                [organization_id: "org", service: "service"]
            ]]

        when:
        Optional<PartnerMetadata> result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == expectedMetadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(missingReceiverMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(expectedMetadata)
    }

    def "setMetadataStatus sets status to Pending"(){
        given:
        def submissionId = "13425"
        def metadataStatus = PartnerMetadataStatus.PENDING
        def optional = Optional.of(new PartnerMetadata("","","","",Instant.now(),"",PartnerMetadataStatus.FAILED))
        mockPartnerMetadataStorage.readMetadata(submissionId) >> optional

        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatus(submissionId,metadataStatus)

        then:
        1 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata) >> { PartnerMetadata partnerMetadata ->
            assert partnerMetadata.deliveryStatus() == metadataStatus
        }
    }

    def "setMetadataStatus doesn't update status if status is the same"(){
        given:
        def submissionId = "13425"
        def metadataStatus = PartnerMetadataStatus.PENDING
        def optional = Optional.of(new PartnerMetadata("","","","",Instant.now(),"",metadataStatus))
        mockPartnerMetadataStorage.readMetadata(submissionId) >> optional

        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatus(submissionId,metadataStatus)

        then:
        0 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata)
    }

    def "setMetadataStatus doesn't update when submissionId is null"(){
        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatus(null,PartnerMetadataStatus.DELIVERED)

        then:
        0 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata)
    }

    def "setMetadataStatus sets status to Pending when there is no metadata"(){
        given:
        def submissionId = "13425"
        def metadataStatus = PartnerMetadataStatus.DELIVERED
        def optional = Optional.empty()
        mockPartnerMetadataStorage.readMetadata(submissionId) >> optional

        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatus(submissionId,metadataStatus)

        then:
        1 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata) >> { PartnerMetadata partnerMetadata ->
            assert partnerMetadata.deliveryStatus() == metadataStatus
            assert partnerMetadata.receivedSubmissionId() == submissionId
        }
    }

    def "getReceiverAndStatus returns correct status name and receiver name from valid JSON response"() {
        given:
        def organization = "org_id"
        def sender = "service_name"
        def status = "Not Delivering"
        def validJson = """{"overallStatus": "${status}", "destinations": [{"organization_id": "${organization}", "service": "${sender}"}]}"""

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def parsedResponse = PartnerMetadataOrchestrator.getInstance().getReceiverAndStatus(validJson)

        then:
        parsedResponse[0] == "${organization}.${sender}"
        parsedResponse[1] == status
    }

    def "getReceiverAndStatus throws FormatterProcessingException or returns null for unexpected format response"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def invalidJson = "invalid JSON"
        PartnerMetadataOrchestrator.getInstance().getReceiverAndStatus(invalidJson)

        then:
        thrown(FormatterProcessingException)

        when:
        def emptyJson = "{}"
        PartnerMetadataOrchestrator.getInstance().getReceiverAndStatus(emptyJson)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithoutDestinations = "{\"someotherkey\": \"value\"}"
        PartnerMetadataOrchestrator.getInstance().getReceiverAndStatus(jsonWithoutDestinations)

        then:
        thrown(FormatterProcessingException)

        when:

        def jsonWithEmptyDestinations = "{\"destinations\": []}"
        def parsedData = PartnerMetadataOrchestrator.getInstance().getReceiverAndStatus(jsonWithEmptyDestinations)

        then:
        parsedData[0] == null

        when:

        def jsonWithNoStatus = "{\"destinations\": []}"
        parsedData = PartnerMetadataOrchestrator.getInstance().getReceiverAndStatus(jsonWithNoStatus)

        then:
        parsedData[1] == null

        when:
        def jsonWithoutOrgId = "{\"destinations\":[{\"service\":\"service\"}]}"
        PartnerMetadataOrchestrator.getInstance().getReceiverAndStatus(jsonWithoutOrgId)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithoutService = "{\"destinations\":[{\"organization_id\":\"org_id\"}]}"
        PartnerMetadataOrchestrator.getInstance().getReceiverAndStatus(jsonWithoutService)

        then:
        thrown(FormatterProcessingException)
    }

    def "ourStatusFromReportStreamStatus returns FAILED"() {
        when:
        def ourStatus = PartnerMetadataOrchestrator.getInstance().ourStatusFromReportStreamStatus("Error")

        then:
        ourStatus == PartnerMetadataStatus.FAILED

        when:
        ourStatus = PartnerMetadataOrchestrator.getInstance().ourStatusFromReportStreamStatus("Not Delivering")

        then:
        ourStatus == PartnerMetadataStatus.FAILED
    }

    def "ourStatusFromReportStreamStatus returns DELIVERED"() {
        when:
        def ourStatus = PartnerMetadataOrchestrator.getInstance().ourStatusFromReportStreamStatus("Delivered")

        then:
        ourStatus == PartnerMetadataStatus.DELIVERED
    }

    def "ourStatusFromReportStreamStatus returns PENDING"() {
        when:
        def ourStatus = PartnerMetadataOrchestrator.getInstance().ourStatusFromReportStreamStatus("Waiting to Deliver")

        then:
        ourStatus == PartnerMetadataStatus.PENDING

        when:
        ourStatus = PartnerMetadataOrchestrator.getInstance().ourStatusFromReportStreamStatus("DogCow")

        then:
        ourStatus == PartnerMetadataStatus.PENDING

        when:
        ourStatus = PartnerMetadataOrchestrator.getInstance().ourStatusFromReportStreamStatus(null)

        then:
        ourStatus == PartnerMetadataStatus.PENDING
    }
}
