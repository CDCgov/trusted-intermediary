package gov.hhs.cdc.trustedintermediary.etor

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.auth.AuthRequestValidator
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.etor.demographics.ConvertAndSendLabOrderUsecase
import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsController
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsResponse
import gov.hhs.cdc.trustedintermediary.etor.demographics.UnableToSendLabOrderException
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
        def specifiedEndpoint = new HttpEndpoint("POST", "/v1/etor/demographics")

        when:
        def endpoints = domainRegistration.domainRegistration()

        then:
        !endpoints.isEmpty()
        endpoints.get(specifiedEndpoint) != null
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

        def mockAuthValidator = Mock(AuthRequestValidator)
        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> true

        def mockDemographicsController = Mock(PatientDemographicsController)

        def mockRequestId = "asdf-12341-jkl-7890"

        mockDemographicsController.parseDemographics(_ as DomainRequest) >> new DemographicsMock(mockRequestId, "a patient ID", "demographics")
        mockDemographicsController.constructResponse(_ as PatientDemographicsResponse) >> new DomainResponse(418)

        def mockUsecase = Mock(ConvertAndSendLabOrderUsecase)

        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        TestApplicationContext.register(PatientDemographicsController, mockDemographicsController)
        TestApplicationContext.register(ConvertAndSendLabOrderUsecase, mockUsecase)
        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
        TestApplicationContext.injectRegisteredImplementations()

        def domainRequest = new DomainRequest()

        when:
        domainRegistration.handleDemographics(domainRequest)

        then:
        1 * mockDemographicsController.constructResponse(_ as PatientDemographicsResponse) >> { PatientDemographicsResponse demographicsResponse ->
            assert demographicsResponse.fhirResourceId == mockRequestId
        }
        1 * mockUsecase.convertAndSend(_ as Demographics)
    }

    def "handleOrder generates an error response when the usecase throws an exception"() {
        given:
        def domainRegistration = new EtorDomainRegistration()
        def mockDemographicConrollor = Mock(PatientDemographicsController)
        def domainRequest = new DomainRequest()
        def mockLabOrderUseCase = Mock(ConvertAndSendLabOrderUsecase)
        def mockAuthValidator = Mock(AuthRequestValidator)
        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> true
        mockLabOrderUseCase.convertAndSend(_) >> {
            throw new UnableToSendLabOrderException("error", new NullPointerException())
        }
        mockDemographicConrollor.constructResponse(_ as Integer, _ as Exception) >> new DomainResponse(400)
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        TestApplicationContext.register(PatientDemographicsController, mockDemographicConrollor)
        TestApplicationContext.register(ConvertAndSendLabOrderUsecase, mockLabOrderUseCase)
        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleDemographics(domainRequest)

        then:
        res.statusCode == 400
    }

    def "demographics endpoint fails with a 401 when unauthenticated"() {
        given:
        def domainRegistration = new EtorDomainRegistration()

        def mockAuthValidator = Mock(AuthRequestValidator)
        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> false

        def mockDemographicsController = Mock(PatientDemographicsController)

        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        TestApplicationContext.register(PatientDemographicsController, mockDemographicsController)
        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleDemographics(new DomainRequest())

        then:
        1 * mockDemographicsController.constructResponse(_ as Integer, _ as String) >> { Integer httpStatus, String errorString ->
            assert httpStatus == 401
        }
        0 * mockDemographicsController.parseDemographics(_)
    }

    def "demographics endpoint fails with a 500 when the authentication checking completely fails"() {
        given:
        def domainRegistration = new EtorDomainRegistration()

        def mockAuthValidator = Mock(AuthRequestValidator)
        mockAuthValidator.isValidAuthenticatedRequest(_ as DomainRequest) >> { throw new SecretRetrievalException("DogCow", new NullPointerException()) }

        def mockDemographicsController = Mock(PatientDemographicsController)

        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        TestApplicationContext.register(PatientDemographicsController, mockDemographicsController)
        TestApplicationContext.register(AuthRequestValidator, mockAuthValidator)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleDemographics(new DomainRequest())

        then:
        1 * mockDemographicsController.constructResponse(_ as Integer, _ as Exception) >> { Integer httpStatus, Exception exception ->
            assert httpStatus == 500
        }
        0 * mockDemographicsController.parseDemographics(_)
    }
}
