package gov.hhs.cdc.trustedintermediary.etor.metadata.partner

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrderConverter
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import java.time.Instant
import spock.lang.Specification

class PartnerMetadataOrchestratorTest extends Specification {

    def mockPartnerMetadataStorage
    def mockMessageLinkStorage
    def mockClient
    def mockFormatter
    def receivedSubmissionId = "receivedSubmissionId"
    def sentSubmissionId = "sentSubmissionId"
    def hashCode = "hash"
    def bearerToken = "token"
    def placerOrderNumber = "placer_order_number"
    def timeReceived = Instant.now()
    def timeDelivered = null
    def messageType = PartnerMetadataMessageType.RESULT
    def failureReason = "failureReason"
    def deliveryStatus = PartnerMetadataStatus.PENDING
    private MessageHdDataType sendingApp
    private MessageHdDataType sendingFacility
    private MessageHdDataType receivingApp
    private MessageHdDataType receivingFacility
    private PartnerMetadata testMetadata

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        mockPartnerMetadataStorage = Mock(PartnerMetadataStorage)
        mockMessageLinkStorage = Mock(MessageLinkStorage)
        mockFormatter = Mock(Formatter)
        mockClient = Mock(RSEndpointClient)

        sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

        testMetadata = new PartnerMetadata(receivedSubmissionId,
                sentSubmissionId,
                timeReceived,
                timeDelivered,
                hashCode,
                deliveryStatus,
                failureReason,
                messageType,
                sendingApp,
                sendingFacility,
                receivingApp,
                receivingFacility,
                placerOrderNumber
                )

        TestApplicationContext.register(PartnerMetadataOrchestrator, PartnerMetadataOrchestrator.getInstance())
        TestApplicationContext.register(MessageLinkStorage, mockMessageLinkStorage)
        TestApplicationContext.register(PartnerMetadataStorage, mockPartnerMetadataStorage)

        TestApplicationContext.register(RSEndpointClient, mockClient)
        TestApplicationContext.register(Formatter, mockFormatter)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "updateMetadataForReceivedMessage updates metadata successfully"() {
        given:

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        def timestamp = "2020-01-01T00:00:00.000Z"

        def rsDeliveryApiResponse = """
            {
                "deliveryId": 20,
                "batchReadyAt": "2024-04-09T18:19:00.431Z",
                "expires": "2024-05-09T18:19:00.431Z",
                "receiver": "flexion.etor-service-receiver-orders",
                "receivingOrgSvcStatus": null,
                "reportId": "ddfeb4e2-af58-433e-9297-a4be01957225",
                "topic": "etor-ti",
                "reportItemCount": 2,
                "fileName": "fhir-transform-sample.yml-ddfeb4e2-af58-433e-9297-a4be01957225-20240409181900.fhir",
                "fileType": "FHIR",
                "originalIngestion": [
                    {
                        "reportId": "2f5f17e7-2161-44d9-b091-2d53c10f6e90",
                        "ingestionTime": "${timestamp}",
                        "sendingOrg": "Clarus Doctors"
                    },
                    {
                        "reportId": "e18c283e-e2e4-4804-bca3-33afe32e6b69",
                        "ingestionTime": "2024-04-09T18:18:00.553Z",
                        "sendingOrg": "DogCow Associates"
                    }
                ]
            }
        """

        def expectedMetadata = new PartnerMetadata(
                receivedSubmissionId,
                sentSubmissionId,
                Instant.parse(timestamp),
                timeDelivered,
                hashCode,
                deliveryStatus,
                failureReason,
                messageType,
                sendingApp,
                sendingFacility,
                receivingApp,
                receivingFacility,
                placerOrderNumber
                )

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(testMetadata)

        then:
        1 * mockClient.getRsToken() >> bearerToken
        1 * mockClient.requestDeliveryEndpoint(receivedSubmissionId, bearerToken) >> rsDeliveryApiResponse
        1 * mockPartnerMetadataStorage.saveMetadata(expectedMetadata)
    }

    def "updateMetadataForSentMessage test case when sentSubmissionId is null"() {
        when:
        def sentSubmissionId = null
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentMessage(receivedSubmissionId, sentSubmissionId)

        then:
        0 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId)
    }

    def "updateMetadataForSentMessage test case when PartnerMetadata returns no data"() {
        given:
        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.empty()

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentMessage(receivedSubmissionId, sentSubmissionId)

        then:
        0 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata)
    }

    def "updateMetadataForSentMessage ends when sentSubmissionId matches the one provided by PartnerMetadata"() {
        given:
        def optional = Optional.of(new PartnerMetadata(receivedSubmissionId, sentSubmissionId, Instant.now(), null, "", PartnerMetadataStatus.FAILED, null, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber))
        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> optional

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

    def "updateMetadataForReceivedMessage throws PartnerMetadataException on client error"() {
        given:
        mockClient.getRsToken() >> "token"
        mockClient.requestDeliveryEndpoint(_ as String, _ as String) >> { throw new ReportStreamEndpointClientException("Client error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(testMetadata)

        then:
        1 * mockPartnerMetadataStorage.saveMetadata(_ as PartnerMetadata) >> { PartnerMetadata metadata ->
            assert metadata.receivedSubmissionId() == receivedSubmissionId
        }
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedMessage throws PartnerMetadataException on formatter error"() {
        given:
        def rsDeliveryApiResponse = "{ASDF}"

        mockClient.getRsToken() >> "token"
        mockClient.requestDeliveryEndpoint(_ as String, _ as String) >> rsDeliveryApiResponse
        mockFormatter.convertJsonToObject(rsDeliveryApiResponse, _ as TypeReference) >> { throw new FormatterProcessingException("Formatter error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(testMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedMessage throws PartnerMetadataException on formatter error due to unexpected response format"() {
        given:
        def wrongFormatResponse = "{\"someotherkey\": \"value\"}"

        mockClient.getRsToken() >> "token"
        mockClient.requestDeliveryEndpoint(_ as String, _ as String) >> wrongFormatResponse
        mockFormatter.convertJsonToObject(wrongFormatResponse, _ as TypeReference) >> [someotherkey: "value"]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(testMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedMessage throws PartnerMetadataException due to 0 originalIngestions"() {
        given:
        def wrongFormatResponse = "{\"originalIngestion\": []}"

        mockClient.getRsToken() >> "token"
        mockClient.requestDeliveryEndpoint(_ as String, _ as String) >> wrongFormatResponse
        mockFormatter.convertJsonToObject(wrongFormatResponse, _ as TypeReference) >> [originalIngestion: []]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(testMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedMessage throws PartnerMetadataException due to null originalIngestion"() {
        given:
        def wrongFormatResponse = "{\"someOtherKey\": {}}"

        mockClient.getRsToken() >> "token"
        mockClient.requestDeliveryEndpoint(_ as String, _ as String) >> wrongFormatResponse
        mockFormatter.convertJsonToObject(wrongFormatResponse, _ as TypeReference) >> [someOtherKey:{}]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(testMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForReceivedMessage throws PartnerMetadataException due to empty originalIngestion"() {
        given:
        def wrongFormatResponse = "{\"originalIngestion\": {}}"

        mockClient.getRsToken() >> "token"
        mockClient.requestDeliveryEndpoint(_ as String, _ as String) >> wrongFormatResponse
        mockFormatter.convertJsonToObject(wrongFormatResponse, _ as TypeReference) >> [originalIngestion:[]]

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForReceivedMessage(testMetadata)

        then:
        thrown(PartnerMetadataException)
    }

    def "updateMetadataForSentMessage updates metadata successfully"() {
        given:
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "hash", PartnerMetadataMessageType.ORDER, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)
        def updatedPartnerMetadata = partnerMetadata.withSentSubmissionId(sentSubmissionId)

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentMessage(receivedSubmissionId, sentSubmissionId)

        then:
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(updatedPartnerMetadata)
    }

    def "updateMetadataForSentMessage test case when sentSubmissionId is null"() {
        given:
        def sentSubmissionId = null

        when:
        PartnerMetadataOrchestrator.getInstance().updateMetadataForSentMessage(receivedSubmissionId, sentSubmissionId)

        then:
        0 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId)
    }

    def "getMetadata throws PartnerMetadataException on client error"() {
        given:
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", Instant.now(), null, "hash", PartnerMetadataStatus.PENDING, "failureReason", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(partnerMetadata)
        mockClient.getRsToken() >> "token"
        mockClient.requestDeliveryEndpoint(_ as String, _ as String) >> { throw new ReportStreamEndpointClientException("Client error", new Exception()) }

        when:
        PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        thrown(PartnerMetadataException)
    }

    def "getMetadata throws PartnerMetadataException on formatter error"() {
        given:
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def partnerMetadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", Instant.now(), null, "hash", PartnerMetadataStatus.PENDING, "failureReason", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

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
        def metadata = new PartnerMetadata(receivedSubmissionId, "sentSubmissionId", Instant.now(), null, "hash", PartnerMetadataStatus.DELIVERED, null, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        when:
        def result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == metadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(metadata)
        0 * mockClient.requestHistoryEndpoint(_, _)
    }

    def "getMetadata skips lookup with stale metadata and missing sentSubmissionId"() {
        given:
        def metadata = new PartnerMetadata(receivedSubmissionId, null, Instant.now(), null, "hash", PartnerMetadataStatus.PENDING, null, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

        when:
        PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(metadata)
        0 * mockClient.requestHistoryEndpoint(_, _)
        notThrown(PartnerMetadataException)
    }

    def "getMetadata retrieves metadata successfully when receiver is present and sentSubmissionId is missing"() {
        given:
        def metadata = new PartnerMetadata(receivedSubmissionId, null, Instant.now(), null, "hash", PartnerMetadataStatus.DELIVERED, null, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        when:
        def result = PartnerMetadataOrchestrator.getInstance().getMetadata(receivedSubmissionId)

        then:
        result.isPresent()
        result.get() == metadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(metadata)
    }

    def "getMetadata gets receiver if missing from metadata"() {
        given:
        def timeDelivered = Instant.now()
        def rsHistoryApiResponse = "{\"actualCompletionAt\": \"2023-10-24T19:48:26.921Z\",\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def receivingFacilityWithMissingUniversalId = new MessageHdDataType("receiving_app_name", null, "receiving_app_type")
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, "", messageType, sendingApp, sendingFacility, receivingApp, receivingFacilityWithMissingUniversalId, placerOrderNumber)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, "", messageType, sendingApp, sendingFacility, receivingApp, receivingFacilityWithMissingUniversalId, placerOrderNumber)

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
        def rsHistoryApiResponse = "{\"destinations\": [{\"organization_id\": \"org\", \"service\": \"service\"}]}"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, null, hashCode, PartnerMetadataStatus.PENDING, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, null, hashCode, PartnerMetadataStatus.FAILED, "", messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

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
        def timeDelivered = Instant.now()
        def rsHistoryApiResponse = "whatever"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, timeDelivered, hashCode, PartnerMetadataStatus.PENDING, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, timeDelivered, hashCode, PartnerMetadataStatus.DELIVERED, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

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

    def "getMetadata saves pending without delivery time if nobody has delivery times"() {
        given:
        def rsHistoryApiResponse = "whatever"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, null, hashCode, PartnerMetadataStatus.PENDING, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [
            overallStatus: "Pending",
            actualCompletionAt: null,
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
        result.get() == missingReceiverMetadata
        1 * mockPartnerMetadataStorage.readMetadata(receivedSubmissionId) >> Optional.of(missingReceiverMetadata)
        1 * mockPartnerMetadataStorage.saveMetadata(missingReceiverMetadata)
    }

    def "getMetadata saves loaded delivered metadata if found"() {
        given:
        def rsHistoryApiResponse = "whatever"
        def missingReceiverMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, null, hashCode, PartnerMetadataStatus.PENDING, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)
        def expectedMetadata = new PartnerMetadata(receivedSubmissionId, sentSubmissionId, timeReceived, null, hashCode, PartnerMetadataStatus.DELIVERED, null, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber)

        mockClient.getRsToken() >> bearerToken
        mockClient.requestHistoryEndpoint(sentSubmissionId, bearerToken) >> rsHistoryApiResponse
        mockFormatter.convertJsonToObject(rsHistoryApiResponse, _ as TypeReference) >> [
            overallStatus: "Delivered",
            actualCompletionAt: null,
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
        def optional = Optional.of(new PartnerMetadata("", "", Instant.now(), null, "", PartnerMetadataStatus.PENDING, "Bad Message", PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber))
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
        def optional = Optional.of(new PartnerMetadata("", "", Instant.now(), null, "", PartnerMetadataStatus.FAILED, null, PartnerMetadataMessageType.RESULT, sendingApp, sendingFacility, receivingApp, receivingFacility, placerOrderNumber))
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
        def status = "Error"
        def errorMessage = "Bad message"
        def validJson = """{"overallStatus": "${status}", "errors": [{"message": "${errorMessage}" }]}"""

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def parsedResponse = PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(validJson)

        then:
        parsedResponse[0] == status
        parsedResponse[1].contains(errorMessage)
    }

    def "getDataFromReportStream throws FormatterProcessingException or returns null for unexpected format response"() {
        given:
        def exception
        def objectMapperMessage = "objectMapper failed to convert"
        def noStatusMessage = "Unable to extract overallStatus"
        def noReasonMessage = "Unable to extract failure reason"
        def noTimeMessage = "Unable to extract timeDelivered"

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def invalidJson = "invalid JSON"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(invalidJson)

        then:
        exception = thrown(FormatterProcessingException)
        exception.getMessage().indexOf(objectMapperMessage) >= 0

        when:
        def emptyJson = "{}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(emptyJson)

        then:
        exception = thrown(FormatterProcessingException)
        exception.getMessage().indexOf(noReasonMessage) >= 0

        when:
        def jsonWithoutDestinations = "{\"someotherkey\": \"value\"}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithoutDestinations)

        then:
        exception = thrown(FormatterProcessingException)
        exception.getMessage().indexOf(noReasonMessage) >= 0

        when:
        def jsonWithNoStatus = """{"destinations": [], "errors": []}"""
        def parsedData = PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithNoStatus)

        then:
        parsedData[0] == null

        when:
        def jsonWithoutErrorMessageSubString = "{\"destinations\":[{\"organization_id\":\"org_id\", \"service\":\"service\"}], \"overallStatus\": \"Error\"}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithoutErrorMessageSubString)

        then:
        exception = thrown(FormatterProcessingException)
        exception.getMessage().indexOf(noReasonMessage) >= 0

        when:
        def jsonWithBadCompletionDate = "{\"actualCompletionAt\": 123, \"destinations\":[{\"organization_id\":\"org_id\", \"service\":\"service\"}], \"overallStatus\": \"Error\", \"errors\": []}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithBadCompletionDate)

        then:
        exception = thrown(FormatterProcessingException)
        exception.getMessage().indexOf(noTimeMessage) >= 0

        when:
        def jsonWithBadStatus = "{\"overallStatus\": 123, \"destinations\":[{\"organization_id\":\"org_id\", \"service\":\"service\"}]}"
        PartnerMetadataOrchestrator.getInstance().getDataFromReportStream(jsonWithBadStatus)

        then:
        exception = thrown(FormatterProcessingException)
        exception.getMessage().indexOf(noStatusMessage) >= 0
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
        def failure = "This thing is bonked"
        def status = PartnerMetadataStatus.PENDING
        def mockMetadata = [
            new PartnerMetadata(
            "123456789",
            null,
            Instant.now(),
            null,
            null,
            status,
            failure,
            PartnerMetadataMessageType.RESULT,
            sendingApp,
            sendingFacility,
            receivingApp,
            receivingFacility,
            placerOrderNumber
            )
        ]
        mockPartnerMetadataStorage.readMetadataForSender(_ as String) >> mockMetadata

        when:
        def result = PartnerMetadataOrchestrator.getInstance().getConsolidatedMetadata("sender")

        then:
        !result.isEmpty()
        result["123456789"]["status"] == status.toString()
        result["123456789"]["stale"] == true
        result["123456789"]["failureReason"] == failure
    }

    def "findMessagesIdsToLink returns a list of message ids"() {
        given:
        def placerOrderNumber = "placerOrderNumber"
        def receivedSubmissionId1 = "1"
        def receivedSubmissionId2 = "2"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def partnerMetadata1 = new PartnerMetadata(receivedSubmissionId1, "hash1", PartnerMetadataMessageType.ORDER, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, placerOrderNumber)
        def partnerMetadata2 = new PartnerMetadata(receivedSubmissionId2, "hash2", PartnerMetadataMessageType.RESULT, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, placerOrderNumber)
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
        def existingLinkId = UUID.randomUUID()
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
            ml.getLinkId() != null && ml.getMessageIds() == messageIdsToLink
        })
    }

    def "linkMessages uses existing link if one exists"() {
        given:
        def existingLinkId = UUID.randomUUID()
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
