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

        then:
        res.statusCode == 400
    }

    // TODO: test should be moved to DomainsRegistrationTest
    //    def "demographics endpoint fails with a 401 when unauthenticated"() {
    //        given:
    //        def domainRegistration = new EtorDomainRegistration()
    //
    //        def mockAuthValidator = Mock(AuthRequestValidator)
    //        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> false
    //
    //        def mockDemographicsController = Mock(PatientDemographicsController)
    //
    //        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
    //        TestApplicationContext.register(PatientDemographicsController, mockDemographicsController)
    //        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
    //        TestApplicationContext.injectRegisteredImplementations()
    //
    //        when:
    //        domainRegistration.handleDemographics(new DomainRequest())
    //
    //        then:
    //        1 * mockDemographicsController.constructResponse(_ as Integer, _ as String) >> { Integer httpStatus, String errorString ->
    //            assert httpStatus == 401
    //        }
    //        0 * mockDemographicsController.parseDemographics(_)
    //    }

    // TODO: test should be moved to DomainsRegistrationTest
    //    def "demographics endpoint fails with a 500 when the authentication checking completely fails"() {
    //        given:
    //        def domainRegistration = new EtorDomainRegistration()
    //        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
    //
    //        def mockAuthValidator = Mock(AuthRequestValidator)
    //        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> { throw new SecretRetrievalException("DogCow", new NullPointerException()) }
    //        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
    //
    //        def mockDemographicsController = Mock(PatientDemographicsController)
    //        TestApplicationContext.register(PatientDemographicsController, mockDemographicsController)
    //
    //        TestApplicationContext.injectRegisteredImplementations()
    //
    //        when:
    //        domainRegistration.handleDemographics(new DomainRequest())
    //
    //        then:
    //        1 * mockDemographicsController.constructResponse(_ as Integer, _ as Exception) >> { Integer httpStatus, Exception exception ->
    //            assert httpStatus == 500
    //        }
    //        0 * mockDemographicsController.parseDemographics(_)
    //    }

    def "Orders endpoint happy path"() {
        given:
        def expected = 200

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
        mockResponseHelper.constructOkResponse(_ as OrdersResponse) >> new DomainResponse(expected)
        TestApplicationContext.register(DomainResponseHelper, mockResponseHelper)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = connector.handleOrders(new DomainRequest())
        def actual = res.statusCode

        then:
        actual == expected
    }


    def "handleOrders generates an error response when the usecase throws an exception"() {
        given:
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

        then:
        res.statusCode == 400
    }

    // TODO: test should be moved to DomainsRegistrationTest
    //    def "Orders endpoint validator returns false unhappy path" () {
    //        given:
    //        def expected = 401
    //
    //        def connector = new EtorDomainRegistration()
    //        TestApplicationContext.register(EtorDomainRegistration, connector)
    //
    //        def mockUseCase = Mock(SendLabOrderUsecase)
    //        TestApplicationContext.register(SendLabOrderUsecase, mockUseCase)
    //
    //        def mockController = Mock(OrdersController)
    //        TestApplicationContext.register(OrdersController, mockController)
    //
    //        def mockAuthValidator = Mock(AuthRequestValidator)
    //        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> false
    //        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
    //
    //        TestApplicationContext.injectRegisteredImplementations()
    //
    //        when:
    //        def res = connector.handleOrders(new DomainRequest())
    //        def actual = res.statusCode
    //
    //        then:
    //        actual == expected
    //    }

    // TODO: test should be moved to DomainsRegistrationTest
    //    def "Orders endpoint validator throws SecretRetrievalException unhappy path"() {
    //        given:
    //        def mockAuthValidator = Mock(AuthRequestValidator)
    //        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
    //        def expected = 500
    //        def connector = new EtorDomainRegistration()
    //        TestApplicationContext.register(EtorDomainRegistration, connector)
    //        def req = new DomainRequest()
    //        TestApplicationContext.injectRegisteredImplementations()
    //        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> {
    //            throw new SecretRetrievalException("DogCaow", new NullPointerException())
    //        }
    //
    //        when:
    //
    //        def res = connector.handleOrders(req)
    //        def actual = res.statusCode
    //
    //        then:
    //        actual == expected
    //    }

    // TODO: test should be moved to DomainsRegistrationTest
    //    def "Orders endpoint validator throws IllegalArgumentException unhappy path"() {
    //        given:
    //        def mockAuthValidator = Mock(AuthRequestValidator)
    //        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
    //        def expected = 500
    //        def connector = new EtorDomainRegistration()
    //        TestApplicationContext.register(EtorDomainRegistration, connector)
    //        def req = new DomainRequest()
    //        TestApplicationContext.injectRegisteredImplementations()
    //        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> {
    //            throw new IllegalArgumentException("DogCaow", new NullPointerException())
    //        }
    //
    //        when:
    //
    //        def res = connector.handleOrders(req)
    //        def actual = res.statusCode
    //        println("status code: " + actual)
    //
    //        then:
    //        actual == expected
    //    }
}
