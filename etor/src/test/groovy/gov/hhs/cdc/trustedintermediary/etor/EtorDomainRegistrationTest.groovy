package gov.hhs.cdc.trustedintermediary.etor

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.etor.demographics.ConvertAndSendDemographicsUsecase
import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsController
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsResponse
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import gov.hhs.cdc.trustedintermediary.etor.operationoutcomes.FhirMetadata
import gov.hhs.cdc.trustedintermediary.etor.orders.Order
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderController
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderResponse
import gov.hhs.cdc.trustedintermediary.etor.orders.SendOrderUseCase
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import java.time.Instant
import spock.lang.Specification

class EtorDomainRegistrationTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
    }

    def "domain registration has endpoints"() {
        given:
        def domainRegistration = new EtorDomainRegistration()
        def demographicsEndpoint = new HttpEndpoint("POST", EtorDomainRegistration.DEMOGRAPHICS_API_ENDPOINT, true)
        def ordersEndpoint = new HttpEndpoint("POST", EtorDomainRegistration.ORDERS_API_ENDPOINT, true)
        def metadataEndpoint = new HttpEndpoint("GET", EtorDomainRegistration.METADATA_API_ENDPOINT, true)

        when:
        def endpoints = domainRegistration.domainRegistration()

        then:
        !endpoints.isEmpty()
        endpoints.get(demographicsEndpoint) != null
        endpoints.get(ordersEndpoint) != null
        endpoints.get(metadataEndpoint) != null
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
            throw new UnableToSendOrderException("error", new NullPointerException())
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

    def "Orders endpoint happy path"() {
        given:
        def expectedStatusCode = 200

        def request = new DomainRequest()
        request.headers["recordid"] = "recordId"

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def mockUseCase = Mock(SendOrderUseCase)
        TestApplicationContext.register(SendOrderUseCase, mockUseCase)

        def mockRequestId = "asdf-12341-jkl-7890"
        def orderMock = new OrderMock<?>(mockRequestId, "a patient ID", "demographics")
        def mockController = Mock(OrderController)
        mockController.parseOrders(_ as DomainRequest) >> orderMock
        TestApplicationContext.register(OrderController, mockController)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructOkResponse(_ as OrderResponse) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = connector.handleOrders(request)
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "handleOrders generates an error response when the usecase throws an exception"() {
        given:
        def expectedStatusCode = 400

        def request = new DomainRequest()
        request.headers["recordid"] = "recordId"

        def domainRegistration = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)

        def mockController = Mock(OrderController)
        mockController.parseOrders(_ as DomainRequest) >> new OrderMock<?>(null, null, null)
        TestApplicationContext.register(OrderController, mockController)

        def mockUseCase = Mock(SendOrderUseCase)
        mockUseCase.convertAndSend(_ as Order<?>, _ as String) >> {
            throw new UnableToSendOrderException("error", new NullPointerException())
        }
        TestApplicationContext.register(SendOrderUseCase, mockUseCase)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as Exception) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockPartnerMetadataOrchestrator = Mock(PartnerMetadataOrchestrator)
        TestApplicationContext.register(PartnerMetadataOrchestrator,mockPartnerMetadataOrchestrator)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleOrders(request)
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "handleOrders returns a 400 response when the request is not parseable"() {
        given:
        def expectedStatusCode = 400

        def request = new DomainRequest()
        request.headers["recordid"] = "recordId"

        def domainRegistration = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)

        def mockController = Mock(OrderController)
        mockController.parseOrders(_ as DomainRequest) >> { throw new FhirParseException("DogCow", new NullPointerException()) }
        TestApplicationContext.register(OrderController, mockController)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as Exception) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockPartnerMetadataOrchestrator = Mock(PartnerMetadataOrchestrator)
        TestApplicationContext.register(PartnerMetadataOrchestrator,mockPartnerMetadataOrchestrator)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleOrders(request)
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "handleOrders generates an error response when not being able to send the order even when setMetadataStatus throws an exception"() {
        given:
        def expectedStatusCode = 400

        def request = new DomainRequest()
        request.headers["recordid"] = "recordId"

        def domainRegistration = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)

        def mockController = Mock(OrderController)
        mockController.parseOrders(_ as DomainRequest) >> new OrderMock<?>(null, null, null)
        TestApplicationContext.register(OrderController, mockController)

        def mockUseCase = Mock(SendOrderUseCase)
        mockUseCase.convertAndSend(_ as Order<?>, _ as String) >> {
            throw new UnableToSendOrderException("error", new NullPointerException())
        }
        TestApplicationContext.register(SendOrderUseCase, mockUseCase)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructErrorResponse(expectedStatusCode, _ as UnableToSendOrderException) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockPartnerMetadataOrchestrator = Mock(PartnerMetadataOrchestrator)
        mockPartnerMetadataOrchestrator.setMetadataStatusToFailed(_, PartnerMetadataStatus.FAILED) >> {
            throw new PartnerMetadataException("error")
        }
        TestApplicationContext.register(PartnerMetadataOrchestrator,mockPartnerMetadataOrchestrator)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleOrders(request)
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "handleOrders logs an error and continues the usecase like normal when the metadata unique ID is missing because we want to know when our integration with RS is broken"() {
        given:

        def request = new DomainRequest()
        request.headers["recordid"] = null  //no metadata unique ID

        def domainRegistration = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)

        def mockController = Mock(OrderController)
        TestApplicationContext.register(OrderController, mockController)

        def mockUseCase = Mock(SendOrderUseCase)
        TestApplicationContext.register(SendOrderUseCase, mockUseCase)

        def mockResponseHelper = Mock(DomainResponseHelper)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleOrders(request)

        then:
        1 * mockLogger.logError(_ as String)
        1 * mockController.parseOrders(_ as DomainRequest) >> new OrderMock<?>("DogCow", "Moof", "Clarus")
        1 * mockUseCase.convertAndSend(_, null)
    }

    def "handleOrders logs an error and continues the usecase like normal when the metadata unique ID is empty because we want to know when our integration with RS is broken"() {
        given:
        def request = new DomainRequest()
        request.headers["recordid"] = ""  // empty metadata unique ID

        def domainRegistration = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)

        def mockController = Mock(OrderController)
        TestApplicationContext.register(OrderController, mockController)

        def mockUseCase = Mock(SendOrderUseCase)
        TestApplicationContext.register(SendOrderUseCase, mockUseCase)

        def mockResponseHelper = Mock(DomainResponseHelper)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleOrders(request)

        then:
        1 * mockLogger.logError(_ as String)
        1 * mockController.parseOrders(_ as DomainRequest) >> new OrderMock<?>("DogCow", "Moof", "Clarus")
        1 * mockUseCase.convertAndSend(_, null)
    }

    def "metadata endpoint happy path"() {
        given:
        def expectedStatusCode = 200

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def request = new DomainRequest()
        request.setPathParams(["id": "metadataId"])

        def mockPartnerMetadataOrchestrator = Mock(PartnerMetadataOrchestrator)
        mockPartnerMetadataOrchestrator.getMetadata(_ as String) >> Optional.ofNullable(new PartnerMetadata("receivedSubmissionId", "sender", Instant.now(), "hash", PartnerMetadataStatus.DELIVERED))
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockPartnerMetadataOrchestrator)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructOkResponseFromString(_ as String) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        def mockOrderConverter = Mock(OrderConverter)
        mockOrderConverter.extractPublicMetadataToOperationOutcome(_ as PartnerMetadata) >> Mock(FhirMetadata)
        TestApplicationContext.register(OrderConverter, mockOrderConverter)

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
}
