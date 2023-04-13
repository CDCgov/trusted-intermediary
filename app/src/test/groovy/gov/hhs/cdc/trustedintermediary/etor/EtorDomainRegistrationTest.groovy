package gov.hhs.cdc.trustedintermediary.etor

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.etor.demographics.ConvertAndSendLabOrderUsecase
import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsController
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsResponse
import gov.hhs.cdc.trustedintermediary.etor.demographics.UnableToSendLabOrderException
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiDemographics
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

import javax.crypto.NullCipher

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

        def mockDemographicsController = Mock(PatientDemographicsController)

        def mockRequestId = "asdf-12341-jkl-7890"

        mockDemographicsController.parseDemographics(_ as DomainRequest) >> new DemographicsMock(mockRequestId, "a patient ID", "demographics")
        mockDemographicsController.constructResponse(_ as PatientDemographicsResponse) >> new DomainResponse(418)

        def mockUsecase = Mock(ConvertAndSendLabOrderUsecase)

        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        TestApplicationContext.register(PatientDemographicsController, mockDemographicsController)
        TestApplicationContext.register(ConvertAndSendLabOrderUsecase, mockUsecase)
        TestApplicationContext.injectRegisteredImplementations()

        def domainRequest = new DomainRequest()

        when:
        def response = domainRegistration.handleOrder(domainRequest)

        then:
        1 * mockDemographicsController.constructResponse(_ as PatientDemographicsResponse) >> { PatientDemographicsResponse demographicsResponse ->
            assert demographicsResponse.fhirResourceId == mockRequestId
        }
        1 * mockUsecase.convertAndSend(_ as Demographics)
    }

    def "handleOrder catches exception"() {
        given:
        def domainRegistration = new EtorDomainRegistration()
        def mockDemographicConrollor = Mock(PatientDemographicsController)
        def domainRequest = new DomainRequest()
        def mockLabOrderUseCase = Mock(ConvertAndSendLabOrderUsecase)
        mockLabOrderUseCase.convertAndSend(_) >> {
            throw new UnableToSendLabOrderException("error", new NullPointerException())
        }
        mockDemographicConrollor.constructResponse(_ as Integer, _ as Exception) >> new DomainResponse(400)
        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        TestApplicationContext.register(PatientDemographicsController, mockDemographicConrollor)
        TestApplicationContext.register(ConvertAndSendLabOrderUsecase, mockLabOrderUseCase)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def res = domainRegistration.handleOrder(domainRequest)

        then:
        res.statusCode == 400
    }
}
