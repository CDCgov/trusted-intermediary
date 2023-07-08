package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException
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
        def expectedBundle = new Bundle()
        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class) >> expectedBundle
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(new DomainRequest())

        then:
        patientDemographics.getUnderlyingDemographics() == expectedBundle
    }

    def "parseDemographics throws an exception when unable to parse de request"() {
        given:
        def controller = PatientDemographicsController.getInstance()
        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class)  >> { throw new FhirParseException("DogCow", new NullPointerException()) }
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        controller.parseDemographics(new DomainRequest())

        then:
        thrown(FhirParseException)
    }
}
