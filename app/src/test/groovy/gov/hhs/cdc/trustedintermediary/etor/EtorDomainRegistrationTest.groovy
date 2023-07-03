package gov.hhs.cdc.trustedintermediary.etor

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.LabOrdersMock
import gov.hhs.cdc.trustedintermediary.auth.AuthRequestValidator
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.etor.demographics.ConvertAndSendDemographicsUsecase
import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsController
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsResponse
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrder
import gov.hhs.cdc.trustedintermediary.etor.orders.OrdersController
import gov.hhs.cdc.trustedintermediary.etor.orders.OrdersResponse
import gov.hhs.cdc.trustedintermediary.etor.orders.SendLabOrderUsecase
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendLabOrderException
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException
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

        when:
        def endpoints = domainRegistration.domainRegistration()

        then:
        !endpoints.isEmpty()
        endpoints.get(demographicsEndpoint) != null
        endpoints.get(ordersEndpoint) != null
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
            throw new UnableToSendLabOrderException("error", new NullPointerException())
        }
        TestApplicationContext.register(ConvertAndSendDemographicsUsecase, mockUseCase)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructErrorResponse(_ as Integer, _ as Exception) >> new DomainResponse(400)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleDemographics(new DomainRequest())
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "Orders endpoint happy path"() {
        given:
        def expectedStatusCode = 200

        def connector = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, connector)

        def mockUseCase = Mock(SendLabOrderUsecase)
        TestApplicationContext.register(SendLabOrderUsecase, mockUseCase)

        def mockRequestId = "asdf-12341-jkl-7890"
        def labOrdersMock = new LabOrdersMock<?>(mockRequestId, "a patient ID", "demographics")
        def mockController = Mock(OrdersController)
        mockController.parseOrders(_ as DomainRequest) >> labOrdersMock
        TestApplicationContext.register(OrdersController, mockController)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructOkResponse(_ as OrdersResponse) >> new DomainResponse(expectedStatusCode)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = connector.handleOrders(new DomainRequest())
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }

    def "handleOrders generates an error response when the usecase throws an exception"() {
        given:
        def expectedStatusCode = 400

        def domainRegistration = new EtorDomainRegistration()
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)

        def mockController = Mock(OrdersController)
        mockController.parseOrders(_ as DomainRequest) >> new LabOrdersMock<?>(null, null, null)
        TestApplicationContext.register(OrdersController, mockController)

        def mockUseCase = Mock(SendLabOrderUsecase)
        mockUseCase.send(_ as LabOrder<?>) >> {
            throw new UnableToSendLabOrderException("error", new NullPointerException())
        }
        TestApplicationContext.register(SendLabOrderUsecase, mockUseCase)

        def mockResponseHelper = Mock(DomainResponseHelper)
        mockResponseHelper.constructErrorResponse(_ as Integer, _ as Exception) >> new DomainResponse(400)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleOrders(new DomainRequest())
        def actualStatusCode = res.statusCode

        then:
        actualStatusCode == expectedStatusCode
    }
}
