package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class RemovePatientIdentifierTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemovePatientIdentifiers()
    }

    def "remove PID.3-4 and PID.3-5 from Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def pid3_4 = FhirBundleHelper.getPID3_4Value(bundle)
        def pid3_5 = FhirBundleHelper.getPID3_5Value(bundle)

        expect:
        pid3_4 != null
        pid3_5 != null

        when:
        transformClass.transform(fhirResource, null)

        then:
        FhirBundleHelper.getPID3_4Value(bundle) == null
        FhirBundleHelper.getPID3_5Value(bundle) == null
    }

    def "don't throw exception if patient resource not present"() {
        given:
        def bundle = new Bundle()
        FhirBundleHelper.createMSHMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        noExceptionThrown()
    }
}
