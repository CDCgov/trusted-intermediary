package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class RemovePatientNameTypeCodeTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemovePatientNameTypeCode()
    }

    def "remove PID.5-7 from Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/002_CA_ORU_R01_initial_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def pid5_7 = HapiHelper.getPID5_7Value(bundle)

        expect:
        pid5_7 != null

        when:
        transformClass.transform(fhirResource, null)

        then:
        HapiHelper.getPID5_7Value(bundle) == null
    }

    def "don't throw exception if patient resource not present"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMSHMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        noExceptionThrown()
    }
}
