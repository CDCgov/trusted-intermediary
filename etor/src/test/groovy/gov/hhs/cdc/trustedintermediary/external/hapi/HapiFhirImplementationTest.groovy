package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

// @todo build actual tests this is a skeleton
class HapiFhirImplementationTest extends Specification {

    Bundle mockBundle
    Patient mockPatient

    def setup() {
        mockBundle = null
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiMessageConverterHelper, HapiMessageConverterHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        mockPatient = new Patient()
        mockBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
    }

    def "parsePath returns null on blank"() {
        given:
        def expectedResult = null
        def result = new HapiResult(expectedResult)

        when:
        def actualResult = result.getUnderlyingResult()

        then:
        actualResult == expectedResult
    }
}
