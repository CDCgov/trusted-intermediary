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
}
