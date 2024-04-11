package gov.hhs.cdc.trustedintermediary.etor

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException
import gov.hhs.cdc.trustedintermediary.etor.demographics.ConvertAndSendDemographicsUsecase
import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsController
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsResponse
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageRequestHandler
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataConverter
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.etor.operationoutcomes.FhirMetadata
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderController
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderResponse
import gov.hhs.cdc.trustedintermediary.etor.orders.SendOrderUseCase
import gov.hhs.cdc.trustedintermediary.etor.results.ResultController
import gov.hhs.cdc.trustedintermediary.etor.results.ResultResponse
import gov.hhs.cdc.trustedintermediary.etor.results.SendResultUseCase
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import java.time.Instant
import spock.lang.Specification

class EtorDomainRegistrationTest extends Specification {

    private sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
    private sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
    private receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
    private receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "domain registration has endpoints"() {
        given:
        def domainRegistration = new EtorDomainRegistration()
        def demographicsEndpoint = new HttpEndpoint("POST", EtorDomainRegistration.DEMOGRAPHICS_API_ENDPOINT, true)
        def ordersEndpoint = new HttpEndpoint("POST", EtorDomainRegistration.ORDERS_API_ENDPOINT, true)
        def metadataEndpoint = new HttpEndpoint("GET", EtorDomainRegistration.METADATA_API_ENDPOINT, true)
        def consolidatedOrdersEndpoint = new HttpEndpoint("GET", EtorDomainRegistration.CONSOLIDATED_SUMMARY_API_ENDPOINT, true)

        when:
        def endpoints = domainRegistration.domainRegistration()

        then:
        !endpoints.isEmpty()
        endpoints.get(demographicsEndpoint) != null
        endpoints.get(ordersEndpoint) != null
        endpoints.get(metadataEndpoint) != null
        endpoints.get(consolidatedOrdersEndpoint) != null
    }

    def "has an OpenAPI specification"() {
        given:
        def domainRegistration = new EtorDomainRegistration()

        when:
        def openApiSpecification = domainRegistration.openApiSpecification()

        then:
        noExceptionThrown()
        !openApiSpecification.isEmpty()
        openApiSpecification.contains("paths:")
    }

    def "correctly handles errors when loading OpenAPI"() {
        given:
        def domainRegistration = Spy(EtorDomainRegistration)
        domainRegistration.openApiStream(_ as String) >> { throw new IOException()}

        when:
        domainRegistration.openApiSpecification()

        then:
        thrown(UnableToReadOpenApiSpecificationException)
    }

    def "openApiStream assertion behaves correctly with bad filenames"() {
        given:
        def domainRegistration = Spy(EtorDomainRegistration)
        domainRegistration.openApiStream(_ as String) >> { throw new IOException()}

        when:
        domainRegistration.openApiStream("badFile")

        then:
        thrown(IOException)
    }

    def "stitches the demographics parsing to the response construction"() {
        given:
        def domainRegistration = new EtorDomainRegistration()

        def mockDemographicsController = Mock(PatientDemographicsController)
        def mockResponseHelper = Mock(DomainResponseHelper)

        def mockRequestId = "asdf-12341-jkl-7890"

        mockDemographicsController.parseDemographics(_ as DomainRequest) >> new DemographicsMock(mockRequestId, "a patient ID", "demographics")
        mockResponseHelper.constructOkResponse(_ as PatientDemographicsResponse) >> new DomainResponse(418)

        def mockUseCase = Mock(ConvertAndSendDemographicsUsecase)

        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        TestApplicationContext.register(PatientDemographicsController, mockDemographicsController)
        TestApplicationContext.register(ConvertAndSendDemographicsUsecase, mockUseCase)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)
        TestApplicationContext.injectRegisteredImplementations()

        def domainRequest = new DomainRequest()

        when:
        domainRegistration.handleDemographics(domainRequest)

        then:
        1 * mockResponseHelper.constructOkResponse(_ as PatientDemographicsResponse) >> { PatientDemographicsResponse demographicsResponse ->
            assert demographicsResponse.fhirResourceId == mockRequestId
        }
        1 * mockUseCase.convertAndSend(_ as Demographics)
    }

    def "handleDemographics generates an error response when the usecase throws an exception"() {
        given:
        def expectedStatusCode = 400

        def domainRegistration = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)

        def mockController = Mock(PatientDemographicsController)
        mockController.parseDemographics(_ as DomainRequest) >> new DemographicsMock<?>(null, null, null)
        TestApplicationContext.register(PatientDemographicsController, mockController)

        def mockUseCase = Mock(ConvertAndSendDemographicsUsecase)
        mockUseCase.convertAndSend(_ as Demographics<?>) >> {
            throw new UnableToSendMessageException("error", new NullPointerException())
        }
        TestApplicationContext.register(ConvertAndSendDemographicsUsecase, mockUseCase)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as Exception) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleDemographics(new DomainRequest())
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "handlesDemographics throws 400 error when a FhirParseException is triggered"() {

        given:
        def expectedStatusCode = 400
        def domainRegistration = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        def mockRequest = new DomainRequest()
        def message = "Something blew up!"
        def cause = new IllegalArgumentException()
        def fhirParseException = new FhirParseException(message, cause)
        def mockPatientDemographicsController = Mock(PatientDemographicsController)
        mockPatientDemographicsController.parseDemographics(mockRequest) >> { throw fhirParseException }
        TestApplicationContext.register(PatientDemographicsController, mockPatientDemographicsController)
        def mockHelper = Mock(DomainResponseHelper)
        mockHelper.constructErrorResponse(expectedStatusCode, fhirParseException) >> { new DomainResponse(expectedStatusCode)}
        TestApplicationContext.register(DomainResponseHelper, mockHelper)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleDemographics(mockRequest)
        def actualStatusCode = res.getStatusCode()

        then:
        actualStatusCode == expectedStatusCode
    }

    def "handleOrders happy path"() {
        given:
        def orderMock = new OrderMock<?>("resource id", "a patient ID", "orders", null, null, null, null, null)
        def request = new DomainRequest(headers: ["recordid": "recordId"])

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def mockUseCase = Mock(SendOrderUseCase)
        TestApplicationContext.register(SendOrderUseCase, mockUseCase)

        def mockController = Mock(OrderController)
        TestApplicationContext.register(OrderController, mockController)

        def mockResponseHelper = Mock(DomainResponseHelper)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        connector.handleOrders(request)

        then:
        1 * mockController.parseOrders(request) >> orderMock
        1 * mockUseCase.convertAndSend(orderMock, _ as String)
        1 * mockResponseHelper.constructOkResponse(_ as OrderResponse)
    }

    def "handleResults happy path"() {
        given:
        def resultMock = new ResultMock<?>("resource id", "lab result", null, null, null, null, null)
        def request = new DomainRequest(headers: ["recordid": "recordId"])

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def mockUseCase = Mock(SendResultUseCase)
        TestApplicationContext.register(SendResultUseCase, mockUseCase)

        def mockController = Mock(ResultController)
        TestApplicationContext.register(ResultController, mockController)

        def mockResponseHelper = Mock(DomainResponseHelper)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        connector.handleResults(request)

        then:
        1 * mockController.parseResults(request) >> resultMock
        1 * mockUseCase.convertAndSend(resultMock, request.headers.get("recordid"))
        1 * mockResponseHelper.constructOkResponse(_ as ResultResponse)
    }

    def "metadata endpoint happy path"() {
        given:
        def expectedStatusCode = 200

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def request = new DomainRequest()
        request.setPathParams(["id": "metadataId"])

        def mockPartnerMetadataOrchestrator = Mock(PartnerMetadataOrchestrator)
        mockPartnerMetadataOrchestrator.getMetadata(_ as String) >> Optional.ofNullable(
                new PartnerMetadata("receivedSubmissionId", "sender", Instant.now(), null,
                "hash", PartnerMetadataStatus.DELIVERED, PartnerMetadataMessageType.ORDER,
                sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number"))
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockPartnerMetadataOrchestrator)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructOkResponseFromString(_ as String) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockPartnerMetadataConverter = Mock(PartnerMetadataConverter)
        mockPartnerMetadataConverter.extractPublicMetadataToOperationOutcome(_ as PartnerMetadata, _ as String) >> Mock(FhirMetadata)
        TestApplicationContext.register(PartnerMetadataConverter, mockPartnerMetadataConverter)

        def mockFhir = Mock(HapiFhir)
        mockFhir.encodeResourceToJson(_) >> ""
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = connector.handleMetadata(request)
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "metadata endpoint returns a 404 response when metadata id is not found"() {
        given:
        def expectedStatusCode = 404

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def request = new DomainRequest()
        request.setPathParams(["id": "metadataId"])

        def mockPartnerMetadataOrchestrator = Mock(PartnerMetadataOrchestrator)
        mockPartnerMetadataOrchestrator.getMetadata(_ as String) >> Optional.empty()
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockPartnerMetadataOrchestrator)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as String) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = connector.handleMetadata(request)
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "metadata endpoint returns a 500 response when there is an exception reading the metadata"() {
        given:
        def expectedStatusCode = 500

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def request = new DomainRequest()
        request.setPathParams(["id": "metadataId"])

        def mockPartnerMetadataOrchestrator = Mock(PartnerMetadataOrchestrator)
        mockPartnerMetadataOrchestrator.getMetadata(_ as String) >> { throw new PartnerMetadataException("DogCow", new Exception()) }
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockPartnerMetadataOrchestrator)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as String) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = connector.handleMetadata(request)
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }


    def "Consolidated metadata endpoint happy path"() {
        given:
        def expectedStatusCode = 200

        def expectedResultMap = ["12345678": ["status": "FAILED", "stale": true, "failureReason": "you done goof"]]

        def request = new DomainRequest()
        request.setPathParams(["sender": "testSender"])

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)


        def mockResponseHelper = Mock(DomainResponseHelper)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
        mockOrchestrator.getConsolidatedMetadata(_ as String) >> expectedResultMap
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = connector.handleConsolidatedSummary(request)
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
        1 * mockResponseHelper.constructOkResponse(expectedResultMap) >> new DomainResponse(expectedStatusCode)
    }

    def "Consolidated metadata endpoint fails with a 500"() {
        given:
        def expectedStatusCode = 500

        def request = new DomainRequest()
        request.setPathParams(["sender": "testSender"])

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def mockResponseHelper = Mock(DomainResponseHelper)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
        mockOrchestrator.getConsolidatedMetadata(_ as String) >> { throw new PartnerMetadataException("woops") }
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = connector.handleConsolidatedSummary(request)

        then:
        response.statusCode == 500
        1 * mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as String) >> new DomainResponse(expectedStatusCode)
    }

    def "handleMessageRequest happy path"() {
        given:
        def expectedStatusCode = 200

        def request = new DomainRequest(headers: ["recordid": "recordId"])

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def requestHandler = Mock(MessageRequestHandler)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = connector.handleMessageRequest(request, requestHandler, _ as String, false)

        then:
        response.statusCode == expectedStatusCode
        1 * requestHandler.handle(_ as String) >> new DomainResponse(expectedStatusCode)
    }

    def "handleMessageRequest returns a 400 response when there is an exception handling the request"() {
        given:
        def expectedStatusCode = 400

        def request = new DomainRequest(headers: ["recordid": "recordId"])
        def response

        def requestHandler = Mock(MessageRequestHandler)

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def mockResponseHelper = Mock(DomainResponseHelper)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        requestHandler.handle(_ as String) >> { throw new FhirParseException("DogCow", new NullPointerException()) }
        response = connector.handleMessageRequest(request, requestHandler, _ as String, false)

        then:
        response.statusCode == expectedStatusCode
        1 * mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as Exception) >> new DomainResponse(expectedStatusCode)
        1 * mockLogger.logError(_ as String, _ as Exception)

        when:
        requestHandler.handle(_ as String) >> { throw new UnableToSendMessageException("DogCow", new NullPointerException()) }
        response = connector.handleMessageRequest(request, requestHandler, _ as String, false)

        then:
        response.statusCode == expectedStatusCode
        1 * mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as Exception) >> new DomainResponse(expectedStatusCode)
        1 * mockLogger.logError(_ as String, _ as Exception)
    }

    def "handleMessageRequest tries to set metadata status as failed when there is an error and required to update metadata"() {
        given:
        def expectedStatusCode = 400

        def request = new DomainRequest(headers: ["recordid": "recordId"])
        def response

        def requestHandler = Mock(MessageRequestHandler)
        requestHandler.handle(_ as String) >> { throw new FhirParseException("DogCow", new NullPointerException()) }

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def mockResponseHelper = Mock(DomainResponseHelper)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockPartnerMetadataOrchestrator = Mock(PartnerMetadataOrchestrator)
        TestApplicationContext.register(PartnerMetadataOrchestrator,mockPartnerMetadataOrchestrator)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        response = connector.handleMessageRequest(request, requestHandler, _ as String, true)

        then:
        response.statusCode == expectedStatusCode
        1 * mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as Exception) >> new DomainResponse(expectedStatusCode)
        1 * mockLogger.logError(_ as String, _ as Exception)
        1 * mockPartnerMetadataOrchestrator.setMetadataStatusToFailed(_ as String, _ as String)

        when:
        mockPartnerMetadataOrchestrator.setMetadataStatusToFailed(_ as String, _ as String) >> {
            throw new PartnerMetadataException("error")
        }
        response = connector.handleMessageRequest(request, requestHandler, _ as String, true)

        then:
        response.statusCode == expectedStatusCode
        1 * mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as Exception) >> new DomainResponse(expectedStatusCode)
        2 * mockLogger.logError(_ as String, _ as Exception)
    }

    def "handleMessageRequest logs an error and continues when the receivedSubmissionId is missing or empty because we want to know when our integration with RS is broken"() {
        given:
        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def requestHandler = Mock(MessageRequestHandler)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        connector.handleMessageRequest(new DomainRequest(), requestHandler, _ as String, false)

        then:
        1 * mockLogger.logError(_ as String)
        1 * requestHandler.handle(null)

        when:
        connector.handleMessageRequest(new DomainRequest(headers: ["recordid": ""]), requestHandler, _ as String, false)

        then:
        1 * mockLogger.logError(_ as String)
        1 * requestHandler.handle(null)
    }
}
