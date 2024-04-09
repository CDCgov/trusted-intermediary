package gov.hhs.cdc.trustedintermediary.etor.metadata.partner

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
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
    private def mockMessageLinkStorage
    private def mockClient
    private def mockFormatter

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        mockPartnerMetadataStorage = Mock(PartnerMetadataStorage)
        mockMessageLinkStorage = Mock(MessageLinkStorage)
        mockFormatter = Mock(Formatter)
        mockClient = Mock(RSEndpointClient)

        TestApplicationContext.register(PartnerMetadataOrchestrator, PartnerMetadataOrchestrator.getInstance())
        TestApplicationContext.register(MessageLinkStorage, mockMessageLinkStorage)
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
        def messageType = PartnerMetadataMessageType.RESULT
        def rsHistoryApiResponse = "{\"actualCompletionAt\": \"2023-10-24T19:48:26.921Z\",\"sender\": \"${sender}\", \"timestamp\": \"${timestamp}\"}"
        def deliveryStatus = PartnerMetadataStatus.PENDING
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def placerOrderNumber = "placer_order_number"

        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [sender: sender, timestamp: timestamp, actualCompletionAt: timeDelivered]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(receivedSubmissionId, hashCode, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        then:
        1 * mockClient.getRsToken() >> bearerToken
        1 * mockClient.requestHistoryEndpoint(receivedSubmissionId, bearerToken) >> rsHistoryApiResponse
        1 * mockPartnerMetadataStorage.saveMetadata(new PartnerMetadata(receivedSubmissionId, sender, Instant.parse(timestamp), null, hashCode, deliveryStatus, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber))
    }

    def "updateMetadataForSentMessage test case when sentSubmissionId is null"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = null

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentMessage(receivedSubmissionId, sentSubmissionId)

        then:
        0 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId)
    }

    def "updateMetadataForSentMessage test case when PartnerMetadata returns no data"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"

        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.empty()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentMessage(receivedSubmissionId, sentSubmissionId)

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
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> { throw new ReportStreamEndpointClientException("Client error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(receivedSubmissionId, "hash", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

        then:
        1 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata) >> { PartnerMetadata metadata ->
            assert metadata.receivedSubmissionId() == receivedSubmissionId
        }
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedOrder throws PartnerMetadataException on formatter error"() {
        given:
        def rsHistoryApiResponse = "{\"sender\": \"responseName\", \"timestamp\": \"2020-01-01T00:00:00.000Z\"}"
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> { throw new FormatterProcessingException("Formatter error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage("receivedSubmissionId", "hash", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedOrder throws PartnerMetadataException on formatter error due to unexpected response format"() {
        given:
        def wrongFormatResponse = "{\"someotherkey\": \"value\"}"
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

        mockClient.getRsToken() >> "token"
        mockClient.requestHistoryEndpoint(_ as String, _ as String) >> wrongFormatResponse
        mockFormatter.convertJsonToObject(wrongFormatResponse, _ as TypeReference) >> [someotherkey: "value"]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage("receivedSubmissionId", "hash", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForSentMessage updates metadata successfully"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sentSubmissionId = "sentSubmissionId"
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sender", Instant.now(), null, "hash", PartnerMetadataStatus.PENDING, PartnerMetadataMessageType.ORDER, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")
        def updatedPartnerMetadata = partnerMetadata.withSentSubmissionId(sentSubmissionId)

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentMessage(receivedSubmissionId, sentSubmissionId)

        then:
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(updatedPartnerMetadata)
    }

    def "getMetadata throws PartnerMetadataException on client error"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", null, Instant.now(), null, "hash", PartnerMetadataStatus.PENDING, "failureReason", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

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
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", null, Instant.now(), null, "hash", PartnerMetadataStatus.PENDING, "failureReason", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

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
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def metadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", "sender", "receiver", Instant.now(), null, "hash", PartnerMetadataStatus.DELIVERED, null, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

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
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def metadata = new PartnerMetadata(receivedSubmissionId, null, "sender", "receiver", Instant.now(), null, "hash", PartnerMetadataStatus.DELIVERED, null, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

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
        def messageType = PartnerMetadataMessageType.RESULT
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def placerOrderNumber = "placer_order_number"
        def rsHistoryApiResponse = "{\"actualCompletionAt\": \"2023-10-24T19:48:26.921Z\",\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, null, timestamp, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, "", messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, "org.service", timestamp, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, "", messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

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
        def messageType = PartnerMetadataMessageType.RESULT
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def placerOrderNumber = "placer_order_number"
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, null, hashCode, PartnerMetadataStatus.PENDING, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, null, hashCode, PartnerMetadataStatus.FAILED, "", messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

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
        def messageType = PartnerMetadataMessageType.RESULT
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def placerOrderNumber = "placer_order_number"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, timeDelivered, hashCode, PartnerMetadataStatus.PENDING, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, sender, receiver, timestamp, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

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
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def optional = Optional.of(new PartnerMetadata("","","","",Instant.now(),null, "",PartnerMetadataStatus.PENDING, "Bad Message", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number"))
        mockPartnerMetadataStorage.readMetadata(submissionId) >> optional

        when:
        PartnerMetadataOrchestrator.getInstance().setMetadataStatusToFailed(submissionId, "Bad Message")

        then:
        1 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata) >> { PartnerMetadata partnerMetadata ->
            assert partnerMetadata.deliveryStatus() == PartnerMetadataStatus.FAILED
        }
    }

    def "setMetadataStatusToFailed doesn't update status if status is the same"() {
        given:
        def submissionId = "13425"
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def optional = Optional.of(new PartnerMetadata("","","","",Instant.now(), null, "", PartnerMetadataStatus.FAILED, null, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number"))
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
        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def mockMetadata = [
            new PartnerMetadata("123456789", null, senderName, "receiver_name", Instant.now(), null, null, status, failure, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")
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

    def "findMessagesIdsToLink returns a list of message ids"() {
        given:
        def receivedSubmissionId = "receivedSubmissionId"
        def placerOrderNumber = "placerOrderNumber"
        def receivedSubmissionId1 = "1"
        def receivedSubmissionId2 = "2"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def partnerMetadata1 = new PartnerMetadata(receivedSubmissionId1, "sender1", Instant.now(), null, "hash1", PartnerMetadataStatus.DELIVERED, PartnerMetadataMessageType.ORDER, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, placerOrderNumber)
        def partnerMetadata2 = new PartnerMetadata(receivedSubmissionId2, "sender2", Instant.now(), null, "hash2", PartnerMetadataStatus.DELIVERED, PartnerMetadataMessageType.RESULT, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, placerOrderNumber)
        def metadataSetForMessageLinking = Set.of(partnerMetadata1, partnerMetadata2)
        mockPartnerMetadataStorage.readMetadataForMessageLinking(receivedSubmissionId) >> metadataSetForMessageLinking

        when:
        def result = PartnerMetadataOrchestrator.getInstance().findMessagesIdsToLink(receivedSubmissionId)

        then:
        result == Set.of(receivedSubmissionId1, receivedSubmissionId2)
    }

    def "linkMessages links messages successfully"() {
        given:
        def matchingMessageId = "matchingMessageId"
        def additionalMessageId = "additionalMessageId"
        def newMessageId = "newMessageId"
        def messageIdsToLink = Set.of(matchingMessageId, newMessageId)
        def existingLinkId = 1
        def existingMessageLink = new MessageLink(existingLinkId, Set.of(matchingMessageId, additionalMessageId))
        mockMessageLinkStorage.getMessageLink(newMessageId) >> Optional.empty()

        when:
        PartnerMetadataOrchestrator.getInstance().linkMessages(messageIdsToLink)

        then:
        1 * mockMessageLinkStorage.getMessageLink(matchingMessageId) >> Optional.of(existingMessageLink)
        existingMessageLink.addMessageId(newMessageId)
        1 * mockMessageLinkStorage.saveMessageLink(existingMessageLink)
    }

    def "linkMessages creates new message link there is no existing link"() {
        given:
        def messageId1 = "messageId1"
        def messageId2 = "messageId2"
        def messageIdsToLink = Set.of(messageId1, messageId2)
        mockMessageLinkStorage.getMessageLink(messageId1) >> Optional.empty()
        mockMessageLinkStorage.getMessageLink(messageId2) >> Optional.empty()

        when:
        PartnerMetadataOrchestrator.getInstance().linkMessages(messageIdsToLink)

        then:
        1 * mockMessageLinkStorage.saveMessageLink({ MessageLink ml ->
            ml.getLinkId() == null && ml.getMessageIds() == messageIdsToLink
        })
    }

    def "linkMessages uses existing link if one exists"() {
        given:
        def existingLinkId = 1
        def matchingMessageId = "messageId"
        def additionalMessageId = "additionalMessageId"
        def newMessageId = "newMessageId"
        def messageIdsToLink = Set.of(matchingMessageId, newMessageId)
        def existingMessageLink = new MessageLink(existingLinkId, Set.of(matchingMessageId, additionalMessageId))
        mockMessageLinkStorage.getMessageLink(matchingMessageId) >> Optional.of(existingMessageLink)
        mockMessageLinkStorage.getMessageLink(newMessageId) >> Optional.empty()

        when:
        PartnerMetadataOrchestrator.getInstance().linkMessages(messageIdsToLink)

        then:
        1 * mockMessageLinkStorage.saveMessageLink({ MessageLink ml ->
            ml.getLinkId() == existingLinkId && ml.getMessageIds() == Set.of(matchingMessageId, additionalMessageId, newMessageId)
        })
    }
}
