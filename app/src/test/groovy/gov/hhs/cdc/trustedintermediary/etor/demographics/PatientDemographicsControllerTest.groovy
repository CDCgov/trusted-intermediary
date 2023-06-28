package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class PatientDemographicsControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PatientDemographicsController, PatientDemographicsController.getInstance())
    }

    def "parseDemographics gets the Bundle and puts it as the underlying demographics"() {
        given:
        def mockBundle = new Bundle()

        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class) >> mockBundle
        TestApplicationContext.register(HapiFhir, fhir)

        TestApplicationContext.injectRegisteredImplementations()

        def request = new DomainRequest()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getUnderlyingDemographics() == mockBundle
    }

    def "demographics constructResponse with PatientDemographicsResponse works"() {
        given:
        def mockBody = "DogCow goes Moof"

        def domainResponseHelper = Mock(DomainResponseHelper)
        domainResponseHelper.constructOkResponse(_ as PatientDemographicsResponse) >> {
            def response = new DomainResponse(200)
            response.setBody(mockBody)
            return response
        }
        TestApplicationContext.register(DomainResponseHelper, domainResponseHelper)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890", "blkjh-7685"))

        then:
        response.getBody() == mockBody
        response.getStatusCode() == 200
    }

    def "demographics constructResponse with error string works"() {
        given:
        def mockStatusCode = 500
        def mockBody = "DogCow goes Moof"
        def response = new DomainResponse(mockStatusCode)
        response.setBody(mockBody)

        def domainResponseHelper = Mock(DomainResponseHelper)
        domainResponseHelper.constructErrorResponse( mockStatusCode, _ as String) >> { response }
        def expected = response

        TestApplicationContext.register(DomainResponseHelper, domainResponseHelper)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PatientDemographicsController.getInstance().constructResponse(mockStatusCode, "error message")

        then:
        actual.getBody() == expected.getBody()
        actual.getStatusCode() == expected.getStatusCode()
    }

    def "demographics constructResponse with exception string works"() {
        given:
        def mockStatusCode = 500
        def mockBody = "DogCow goes Moof"
        def response = new DomainResponse(mockStatusCode)
        response.setBody(mockBody)
        def expected = response

        def domainResponseHelper = Mock(DomainResponseHelper)
        domainResponseHelper.constructErrorResponse(mockStatusCode, _ as Exception) >> response

        TestApplicationContext.register(DomainResponseHelper, domainResponseHelper)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PatientDemographicsController.getInstance().constructResponse(mockStatusCode, new Exception("dogcow"))

        then:
        actual.getBody() == expected.getBody()
        actual.getStatusCode() == expected.getStatusCode()
    }
}
