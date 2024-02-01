package gov.hhs.cdc.trustedintermediary.etor.metadata.partner

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
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
        def timeDelivered = "2020-01-02T00:00:00.000Z"
        def hashCode = "123"
        def bearerToken = "token"
        def rsHistoryApiResponse = "{\"actualCompletionAt\": \"2023-10-24T19:48:26.921Z\",\"sender\": \"${sender}\", \"timestamp\": \"${timestamp}\"}"
        def deliveryStatus = PartnerMetadataStatus.PENDING


        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [sender: sender, timestamp: timestamp, actualCompletionAt: timeDelivered]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedOrder(receivedSubmissionId, hashCode)

        then:
        1 * mockClient.getRsToken() >> bearerToken
        1 * mockClient.requestHistoryEndpoint(receivedSubmissionId, bearerToken) >> rsHistoryApiResponse
        1 * mockPartnerMetadataStorage.saveMetadata(new PartnerMetadata(receivedSubmissionId, sender, Instant.parse(timestamp), null, hashCode, deliveryStatus))
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
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sender", Instant.now(), null, "hash", PartnerMetadataStatus.PENDING)
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
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, "sender", null, Instant.now(), null, "hash", PartnerMetadataStatus.PENDING, "failureReason")

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
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, "sender", null, Instant.now(), null, "hash", PartnerMetadataStatus.PENDING, "failureReason")

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
        def metadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", "receiver", Instant.now(), null, "hash", PartnerMetadataStatus.DELIVERED, null)

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
        def metadata = new PartnerMetadata(receivedSubmissionId, null, "sender", "receiver", Instant.now(), null, "hash", PartnerMetadataStatus.DELIVERED, null)

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
        def timeDelivered = Instant.now()
        def hashCode = "123"
        def bearerToken = "token"
        def rsHistoryApiResponse = "{\"actualCompletionAt\": \"2023-10-24T19:48:26.921Z\",\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, null, timestamp, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, "")
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, "org.service", timestamp, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, "")

        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [
            overallStatus: "Delivered",
            actualCompletionAt: timeDelivered.toString(),
            destinations: [
                [organization_id: "org", service: "service"]
            ],
            errors: []
        ]

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
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, null, hashCode, PartnerMetadataStatus.PENDING, null)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, null, hashCode, PartnerMetadataStatus.FAILED, "")

        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [
            overallStatus: "Not Delivering",
            destinations: [
                [organization_id: "org", service: "service"],
            ],
            errors: [],
        ]

        when:
        Optional<PartnerMetadata> result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == expectedMetadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(missingReceiverMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(expectedMetadata)
    }

    def "getMetadata doesn't update the error messages if the status isn't FAILED when calling the RS history API"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sender = "senderName"
        def receiver = "org.service"
        def timestamp = Instant.now()
        def timeDelivered = Instant.now()
        def hashCode = "123"
        def bearerToken = "token"
        def rsHistoryApiResponse = "whatever"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, timeDelivered, hashCode, PartnerMetadataStatus.PENDING, null)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, null)

        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [
            overallStatus: "Delivered",
            actualCompletionAt: timeDelivered.toString(),
            destinations: [
                [organization_id: "org", service: "service"],
            ],
            errors: [
                [
                    message: "This is an error message"
                ]
            ],
        ]

        when:
        Optional<PartnerMetadata> result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == expectedMetadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(missingReceiverMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(expectedMetadata)
    }

    def "setMetadataStatusToFailed sets status to Failed"() {
        given:
        def submissionId = "13425"
        def metadataStatus = PartnerMetadataStatus.FAILED
        def optional = Optional.of(new PartnerMetadata("","","","",Instant.now(),null, "",PartnerMetadataStatus.PENDING, "Bad Message"))
        mockPartnerMetadataStorage.readMetadata(submissionId) >> optional

        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatusToFailed(submissionId, "Bad Message")

        then:
        1 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata) >> { PartnerMetadata partnerMetadata ->
            assert partnerMetadata.deliveryStatus() == metadataStatus
        }
    }

    def "setMetadataStatusToFailed doesn't update status if status is the same"() {
        given:
        def submissionId = "13425"
        def metadataStatus = PartnerMetadataStatus.FAILED
        def optional = Optional.of(new PartnerMetadata("","","","",Instant.now(), null, "",metadataStatus, null))
        mockPartnerMetadataStorage.readMetadata(submissionId) >> optional

        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatusToFailed(submissionId, null)

        then:
        0 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata)
    }

    def "setMetadataStatusToFailed sets status to Failed when there is no metadata"() {
        given:
        def submissionId = "13425"
        mockPartnerMetadataStorage.readMetadata(submissionId) >> Optional.empty()

        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatusToFailed(submissionId, "Failure")

        then:
        1 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata) >> { PartnerMetadata partnerMetadata ->
            assert partnerMetadata.deliveryStatus() == PartnerMetadataStatus.FAILED
            assert partnerMetadata.receivedSubmissionId() == submissionId
        }
    }

    def "setMetadataStatusToFailed doesn't update when submissionId is null"() {
        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatusToFailed(null, null)

        then:
        0 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata)
    }


    def "getDataFromReportStream returns correct status, receiver name, error messages from valid JSON response"() {
        given:
        def organization = "org_id"
        def sender = "service_name"
        def status = "Error"
        def errorMessage = "Bad message"
        def validJson = """{"overallStatus": "${status}", "destinations": [{"organization_id": "${organization}", "service": "${sender}"}], "errors": [{"message": "${errorMessage}" }]}"""

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def parsedResponse = PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(validJson)

        then:
        parsedResponse[0] == "${organization}.${sender}"
        parsedResponse[1] == status
        parsedResponse[2].contains(errorMessage)
    }

    def "getDataFromReportStream throws FormatterProcessingException or returns null for unexpected format response"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def invalidJson = "invalid JSON"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(invalidJson)

        then:
        thrown(FormatterProcessingException)

        when:
        def emptyJson = "{}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(emptyJson)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithoutDestinations = "{\"someotherkey\": \"value\"}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithoutDestinations)

        then:
        thrown(FormatterProcessingException)

        when:

        def jsonWithEmptyDestinations = """{"destinations": [], "errors": []}"""
        def parsedData = PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithEmptyDestinations)

        then:
        parsedData[0] == null

        when:

        def jsonWithNoStatus = """{"destinations": [], "errors": []}"""
        parsedData = PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithNoStatus)

        then:
        parsedData[1] == null

        when:
        def jsonWithoutOrgId = "{\"destinations\":[{\"service\":\"service\"}]}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithoutOrgId)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithoutService = "{\"destinations\":[{\"organization_id\":\"org_id\"}]}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithoutService)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithoutErrorMessageSubString = "{\"destinations\":[{\"organization_id\":\"org_id\", \"service\":\"service\"}], \"overallStatus\": \"Error\"}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithoutErrorMessageSubString)

        then:
        thrown(FormatterProcessingException)

        when:
        def jsonWithBadCompletionDate = "{\"actualCompletionAt\": 123, \"destinations\":[{\"organization_id\":\"org_id\", \"service\":\"service\"}], \"overallStatus\": \"Error\"}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithBadCompletionDate)
        then:
        thrown(FormatterProcessingException)
        when:
        def jsonWithBadStatus = "{\"overallStatus\": 123, \"destinations\":[{\"organization_id\":\"org_id\", \"service\":\"service\"}]}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithBadStatus)
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

    def "getConsolidatedMetadata populates a map of maps"() {
        given:

        def senderName = "sender_name"
        def failure = "This thing is bonked"
        def status = PartnerMetadataStatus.PENDING
        def mockMetadata = [
            new PartnerMetadata("123456789", null, senderName, "receiver_name", Instant.now(), null, null, status, failure)
        ]
        mockPartnerMetadataStorage.readMetadataForSender(senderName) >> mockMetadata

        when:
        def result = PartnerMetadataOrchestrator.getInstance().getConsolidatedMetadata(senderName)

        then:
        !result.isEmpty()
        result["123456789"]["status"] == status.toString()
        result["123456789"]["stale"] == true
        result["123456789"]["failureReason"] == failure
    }
}
